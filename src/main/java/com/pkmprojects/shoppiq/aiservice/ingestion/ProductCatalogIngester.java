package com.pkmprojects.shoppiq.aiservice.ingestion;

import com.pkmprojects.shoppiq.aiservice.events.ProductEmbeddingEvent;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.service.item.ItemLookupService;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the Qdrant product vector store synchronized with the Shoppiq catalog.
 *
 * <p>This component implements {@link CommandLineRunner} to perform initial
 * vector store synchronization on application startup. It also listens for
 * {@link ProductEmbeddingEvent}s via Spring's transactional event mechanism
 * to handle incremental product updates after their originating transactions
 * commit. The ingester converts product entities into text segments with
 * metadata, computes vector embeddings using the BGE-small-en model, and
 * stores them in the Qdrant vector store for semantic search during AI
 * conversations.</p>
 *
 * <p>On startup, the ingester probes the vector store to determine whether
 * it is empty. If empty (or if the {@code reindex-on-startup} flag is set),
 * a full reindex is performed by iterating through all products in pages of
 * 100. Incremental updates handle product creation, modification, and
 * deletion events, ensuring that the vector store always reflects the
 * current state of the product catalog.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "shoppiq.ai.enabled", havingValue = "true", matchIfMissing = false)
public class ProductCatalogIngester implements CommandLineRunner {

    private static final int PAGE_SIZE = 100;

    private final ItemLookupService itemLookupService;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final boolean reindexOnStartup;
    private final String collectionName;

    public ProductCatalogIngester(ItemLookupService itemLookupService,
                                  EmbeddingModel embeddingModel,
                                  EmbeddingStore<TextSegment> embeddingStore,
                                  @Value("${shoppiq.ai.rag.reindex-on-startup:false}") boolean reindexOnStartup,
                                  @Value("${langchain4j.qdrant.collection-name:shoppiq_products}") String collectionName) {
        this.itemLookupService = itemLookupService;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.reindexOnStartup = reindexOnStartup;
        this.collectionName = collectionName;
    }

    @Override
    public void run(String... args) {
        if (reindexOnStartup) {
            reindexAll();
            return;
        }
        try {
            Embedding probe = embeddingModel.embed("product catalog sync probe").content();
            boolean empty = embeddingStore.search(
                    EmbeddingSearchRequest.builder()
                            .queryEmbedding(probe)
                            .minScore(0.0)
                            .maxResults(1)
                            .build()
            ).matches().isEmpty();
            if (empty) {
                log.debug("[RAG] Vector store '{}' appears empty — running initial reindex", collectionName);
                reindexAll();
            } else {
                log.debug("[RAG] Vector store '{}' already populated — skipping initial reindex", collectionName);
            }
        } catch (Exception e) {
            log.warn("[RAG] Initial vector-store probe failed — running reindex to be safe", e);
            reindexAll();
        }
    }

    /**
     * Rebuilds the entire product vector store from the current catalog.
     *
     * <p>This method clears all existing embeddings from the Qdrant collection
     * and rebuilds them from scratch by iterating through all products in
     * pages of 100. For each product, a text segment is constructed with
     * product details and metadata, embedded using the local BGE model, and
     * stored in the vector store. This operation is used during startup
     * reindexing and can be triggered manually by admins via the reindex
     * endpoint.</p>
     */
    public void reindexAll() {
        log.debug("[RAG] Starting full product catalog reindex → collection '{}'", collectionName);
        embeddingStore.removeAll();

        int pageNumber = 0;
        long total = 0;
        Page<Item> page;
        do {
            page = itemLookupService.findAllWithItemDetails(pageNumber, PAGE_SIZE);

            if (page.hasContent()) {
                List<Item> items = page.getContent();
                List<TextSegment> segments = new ArrayList<>(items.size());
                List<String> ids = new ArrayList<>(items.size());
                for (Item item : items) {
                    ids.add(String.valueOf(item.getId()));
                    segments.add(toTextSegment(item));
                }
                List<Embedding> embeddings = new ArrayList<>(segments.size());
                for (TextSegment segment : segments) {
                    embeddings.add(embeddingModel.embed(segment).content());
                }
                embeddingStore.addAll(ids, embeddings, segments);
                total += items.size();
            }
            pageNumber++;
        } while (page.hasNext());

        log.debug("[RAG] Reindex complete — {} products indexed into '{}'", total, collectionName);
    }

    /**
     * Handles a {@link ProductEmbeddingEvent} after the originating transaction commits.
     *
     * <p>This transactional event listener processes incremental product changes
     * by either upserting or removing the corresponding vector in the Qdrant
     * store. For non-deletion events, a fresh text segment is constructed from
     * the event data, embedded, and stored. For deletion events, the product
     * vector is removed by its item ID. The AFTER_COMMIT phase ensures that
     * vector store updates never interfere with the primary product persistence
     * transaction.</p>
     *
     * @param event the product embedding event carrying item data
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductEvent(ProductEmbeddingEvent event) {
        if (event == null || event.itemId() == null) {
            return;
        }
        String id = String.valueOf(event.itemId());

        if (event.deleted()) {
            embeddingStore.remove(id);
            log.debug("[RAG] Removed product {} from vector store", id);
            return;
        }

        TextSegment segment = toTextSegment(event);
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.addAll(List.of(id), List.of(embedding), List.of(segment));
        log.debug("[RAG] Upserted product {} into vector store", id);
    }

    /**
     * Converts a JPA {@link Item} entity into a {@link TextSegment} with metadata.
     *
     * <p>Constructs a human-readable text representation of the product including
     * name, description, price, category, brand, and stock quantity. Attaches
     * structured metadata (item ID, slug, name, category, price, and in-stock
     * status) that is used for filtering during semantic search. The in-stock
     * metadata field is stored as a string ("true"/"false") to match the
     * Qdrant filter syntax.</p>
     *
     * @param item the product entity
     * @return a text segment with product details and metadata for embedding
     */
    private TextSegment toTextSegment(Item item) {
        var details = item.getItemDetails();
        var category = details != null ? details.getCategory() : null;
        BigDecimal price = details != null ? details.getPrice() : BigDecimal.ZERO;
        int stock = (details != null && details.getStockQuantity() != null)
                ? details.getStockQuantity() : 0;
        String categoryName = category != null ? category.getName() : "Uncategorized";

        String text = "Product: %s%nDescription: %s%nPrice: $%.2f%nCategory: %s%nBrand: %s%nStock: %d units"
                .formatted(
                        orEmpty(item.getName()),
                        orEmpty(item.getDescription()),
                        price.doubleValue(),
                        categoryName,
                        details != null ? orEmpty(details.getBrand()) : "",
                        stock
                );

        Metadata metadata = new Metadata()
                .put("itemId", String.valueOf(item.getId()))
                .put("slug", orEmpty(item.getSlug()))
                .put("name", orEmpty(item.getName()))
                .put("category", category != null ? orEmpty(category.getSlug()) : "none")
                .put("price", price.doubleValue())
                .put("inStock", stock > 0 ? "true" : "false");

        return TextSegment.from(text, metadata);
    }

    /**
     * Converts a {@link ProductEmbeddingEvent} into a {@link TextSegment} with metadata.
     *
     * <p>Constructs the same text and metadata format as the entity-based overload,
     * but operates on event data instead of JPA entities. This allows incremental
     * vector store updates to use the same embedding format as the full reindex,
     * ensuring consistency between startup ingestion and real-time updates.</p>
     *
     * @param event the product embedding event
     * @return a text segment with product details and metadata for embedding
     */
    private TextSegment toTextSegment(ProductEmbeddingEvent event) {
        BigDecimal price = event.price() != null ? event.price() : BigDecimal.ZERO;
        int stock = event.stockQuantity();
        String categoryName = event.categoryName() != null ? event.categoryName() : "Uncategorized";

        String text = "Product: %s%nDescription: %s%nPrice: $%.2f%nCategory: %s%nBrand: %s%nStock: %d units"
                .formatted(
                        orEmpty(event.name()),
                        orEmpty(event.description()),
                        price.doubleValue(),
                        categoryName,
                        orEmpty(event.brand()),
                        stock
                );

        Metadata metadata = new Metadata()
                .put("itemId", String.valueOf(event.itemId()))
                .put("slug", orEmpty(event.slug()))
                .put("name", orEmpty(event.name()))
                .put("category", orEmpty(event.categorySlug()))
                .put("price", price.doubleValue())
                .put("inStock", stock > 0 ? "true" : "false");

        return TextSegment.from(text, metadata);
    }

    /**
     * Returns the given string, or an empty string if {@code null}.
     *
     * <p>Utility method used during text segment construction to ensure that
     * product fields never produce "null" text in the embedded content.</p>
     *
     * @param value the input string
     * @return the original string, or {@code ""} if {@code null}
     */
    private String orEmpty(String value) {
        return value != null ? value : "";
    }
}
