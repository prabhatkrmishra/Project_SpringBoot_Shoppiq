package com.pkmprojects.shoppiq.aiservice.events;

import java.math.BigDecimal;

/**
 * Carries minimal product information needed to build vector-store embeddings.
 *
 * <p>This event class is published by {@link ItemEmbeddingEntityListener}
 * on JPA lifecycle events (post-persist, post-update, pre-remove) and
 * consumed after transaction commit by {@code ProductCatalogIngester}.
 * It decouples the product persistence layer from the RAG infrastructure,
 * ensuring that vector store updates never block or fail the primary
 * product save transaction.</p>
 *
 * <p>The event contains all product fields needed to construct a text
 * segment and metadata for embedding. For deletion events, only the
 * {@code itemId} and {@code deleted} flag are populated; all other
 * fields are null or zero.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ProductEmbeddingEvent(Long itemId, String name, String description, String slug, BigDecimal price,
                                    String categorySlug, String categoryName, String brand, int stockQuantity,
                                    boolean deleted) {

}
