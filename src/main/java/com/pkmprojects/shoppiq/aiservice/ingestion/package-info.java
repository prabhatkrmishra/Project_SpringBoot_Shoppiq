/**
 * Product catalog ingestion for the RAG pipeline.
 *
 * <p>Contains the ingestion component that keeps the Qdrant vector
 * store synchronized with the Shoppiq product catalog. The ingestion
 * process converts product entities into text segments with metadata,
 * computes vector embeddings using the BGE-small-en model, and stores
 * them for semantic search during AI conversations.</p>
 *
 * <p>The ingester performs a full reindex on application startup when
 * the vector store is empty or when explicitly triggered by an admin.
 * Incremental updates are handled via Spring transactional event
 * listeners that respond to product create, update, and delete
 * operations.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.aiservice.ingestion;
