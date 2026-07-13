package com.pkmprojects.shoppiq.aiservice.events;

import com.pkmprojects.shoppiq.entity.Item;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;

/**
 * JPA entity listener that publishes {@link ProductEmbeddingEvent}s whenever a
 * product is created, updated, or deleted.
 *
 * <p>
 * Registered on {@link Item} via {@code @EntityListeners}. The events are
 * consumed by {@code ProductCatalogIngester} in an after-commit transaction
 * phase, keeping the Qdrant vector store in sync with the catalog without
 * coupling the persistence layer to the RAG infrastructure.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public class ItemEmbeddingEntityListener {

    @PostPersist
    @PostUpdate
    public void onUpsert(Item item) {
        if (item == null || item.getItemDetails() == null) {
            return;
        }
        var details = item.getItemDetails();
        var category = details.getCategory();

        ProductEmbeddingEvent event = new ProductEmbeddingEvent(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getSlug(),
                details.getPrice(),
                category != null ? category.getSlug() : "none",
                category != null ? category.getName() : "Uncategorized",
                details.getBrand(),
                details.getStockQuantity() != null ? details.getStockQuantity() : 0,
                false
        );
        ApplicationEventPublisherHolder.publish(event);
    }

    @PreRemove
    public void onRemove(Item item) {
        if (item == null) {
            return;
        }
        ProductEmbeddingEvent event = new ProductEmbeddingEvent(
                item.getId(), null, null, null, null, null, null, null, 0, true);
        ApplicationEventPublisherHolder.publish(event);
    }
}
