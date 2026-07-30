package com.pkmprojects.shoppiq.aiservice.events;

import com.pkmprojects.shoppiq.entity.item.Item;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;

/**
 * JPA entity listener that publishes {@link ProductEmbeddingEvent}s on product lifecycle changes.
 *
 * <p>This listener is registered on the {@code Item} entity and fires on
 * post-persist, post-update, and pre-remove lifecycle callbacks. It
 * extracts the relevant product data and publishes a
 * {@link ProductEmbeddingEvent} via the static
 * {@link ApplicationEventPublisherHolder}, keeping the Qdrant vector
 * store in sync with the product catalog without coupling the persistence
 * layer to RAG infrastructure.</p>
 *
 * <p>For upsert events (create/update), the listener extracts product
 * name, description, price, category, brand, and stock information.
 * For delete events, only the item ID and a deletion flag are included,
 * allowing the consumer to remove the corresponding vector from the
 * store.</p>
 *
 * @author prabhatkrmishra
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
