/**
 * Application events for AI embedding operations.
 *
 * <p>Contains Spring application event classes and JPA entity listeners
 * that keep the Qdrant vector store synchronized with the product
 * catalog. When a product is created, updated, or deleted, the entity
 * listener publishes a {@code ProductEmbeddingEvent} that is consumed
 * after transaction commit to upsert or remove the corresponding
 * vector embedding.</p>
 *
 * <p>This event-driven architecture decouples the core product
 * persistence layer from the RAG infrastructure, ensuring that
 * vector store updates never block or fail the primary transaction.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.aiservice.events;
