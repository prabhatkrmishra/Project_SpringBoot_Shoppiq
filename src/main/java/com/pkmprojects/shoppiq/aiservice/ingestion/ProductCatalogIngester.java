package com.pkmprojects.shoppiq.aiservice.ingestion;

import com.pkmprojects.shoppiq.aiservice.events.ProductEmbeddingEvent;
import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.repository.ItemRepository;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the Qdrant product vector store synchronised with the Shoppiq catalog.
 *
 * <p>
 * On startup it performs an initial reindex when explicitly enabled or when the
 * store is detected empty. Thereafter it reacts to {@link ProductEmbeddingEvent}s
 * (published by {@code ItemEmbeddingEntityListener}) in the after-commit phase,
 * upserting or removing the affected product vector without a full reindex.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "shoppiq.ai.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class ProductCatalogIngester implements CommandLineRunner {

    private static final int PAGE_SIZE = 100;

    private final ItemRepository itemRepository;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Value("${shoppiq.ai.rag.reindex-on-startup:false}")
    private boolean reindexOnStartup;

    @Value("${langchain4j.qdrant.collection-name:shoppiq_products}")
    private String collectionName;

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
                log.info("[RAG] Vector store '{}' appears empty — running initial reindex", collectionName);
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
     */
    public void reindexAll() {
        log.info("[RAG] Starting full product catalog reindex → collection '{}'", collectionName);
        embeddingStore.removeAll();

        int pageNumber = 0;
        long total = 0;
        Page<Item> page;
        do {
            Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE);
            page = itemRepository.findAllWithItemDetails(pageable);

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

        log.info("[RAG] Reindex complete — {} products indexed into '{}'", total, collectionName);
    }

    /**
     * Handles a {@link ProductEmbeddingEvent} after the originating transaction commits.
     *
     * <p>
     * If the event is a deletion, removes the product vector from the store.
     * Otherwise, upserts the product with a freshly computed embedding.
     *
     * @param event the product embedding event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductEvent(ProductEmbeddingEvent event) {
        if (event == null || event.getItemId() == null) {
            return;
        }
        String id = String.valueOf(event.getItemId());

        if (event.isDeleted()) {
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
     * Converts a JPA {@link Item} entity into a {@link TextSegment} with metadata
     * suitable for embedding and storage in the vector store.
     *
     * @param item the product entity
     * @return a text segment with product details and metadata
     */
    private TextSegment toTextSegment(Item item) {
        var details = item.getItemDetails();
        var category = details != null ? details.getCategory() : null;
        BigDecimal price = details != null ? details.getPrice() : BigDecimal.ZERO;
        int stock = (details != null && details.getStockQuantity() != null)
                ? details.getStockQuantity() : 0;
        String categoryName = category != null ? category.getName() : "Uncategorized";

        String text = String.format(
                "Product: %s%nDescription: %s%nPrice: $%.2f%nCategory: %s%nBrand: %s%nStock: %d units",
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
     * Converts a {@link ProductEmbeddingEvent} into a {@link TextSegment} with metadata
     * suitable for embedding and storage in the vector store.
     *
     * @param event the product embedding event
     * @return a text segment with product details and metadata
     */
    private TextSegment toTextSegment(ProductEmbeddingEvent event) {
        BigDecimal price = event.getPrice() != null ? event.getPrice() : BigDecimal.ZERO;
        int stock = event.getStockQuantity();
        String categoryName = event.getCategoryName() != null ? event.getCategoryName() : "Uncategorized";

        String text = String.format(
                "Product: %s%nDescription: %s%nPrice: $%.2f%nCategory: %s%nBrand: %s%nStock: %d units",
                orEmpty(event.getName()),
                orEmpty(event.getDescription()),
                price.doubleValue(),
                categoryName,
                orEmpty(event.getBrand()),
                stock
        );

        Metadata metadata = new Metadata()
                .put("itemId", String.valueOf(event.getItemId()))
                .put("slug", orEmpty(event.getSlug()))
                .put("name", orEmpty(event.getName()))
                .put("category", orEmpty(event.getCategorySlug()))
                .put("price", price.doubleValue())
                .put("inStock", stock > 0 ? "true" : "false");

        return TextSegment.from(text, metadata);
    }

    /**
     * Returns the given string, or an empty string if {@code null}.
     *
     * @param value the input string
     * @return the original string, or {@code ""} if {@code null}
     */
    private String orEmpty(String value) {
        return value != null ? value : "";
    }
}
