package com.pkmprojects.shoppiq.aiservice.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the Retrieval-Augmented Generation (RAG) pipeline with local
 * embeddings and a Qdrant vector store.
 *
 * <p>This configuration class assembles the complete RAG infrastructure
 * for the Shoppiq AI assistant. It initializes the BGE-small-en quantized
 * embedding model (384-dimensional), establishes a gRPC connection to the
 * Qdrant vector database, and creates an {@link EmbeddingStore} backed by
 * the configured collection. The content retriever applies an in-stock
 * filter so that only currently available products are surfaced to the
 * AI model during conversations.</p>
 *
 * <p>The configuration also ensures that the Qdrant collection exists on
 * startup, creating it with cosine distance metrics if it is missing.
 * All connection parameters (host, port, collection name, max results,
 * and minimum similarity score) are externalized to application properties
 * for flexible deployment across environments.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "shoppiq.ai.enabled", havingValue = "true", matchIfMissing = false)
public class RagConfig {

    private static final Logger log = LoggerFactory.getLogger(RagConfig.class);

    private final String host;
    private final int port;
    private final String collectionName;
    private final Integer maxResults;
    private final Double minScore;

    public RagConfig(@Value("${langchain4j.qdrant.host:localhost}") String host,
                     @Value("${langchain4j.qdrant.port:6334}") int port,
                     @Value("${langchain4j.qdrant.collection-name:shoppiq_products}") String collectionName,
                     @Value("${shoppiq.ai.rag.max-results:5}") Integer maxResults,
                     @Value("${shoppiq.ai.rag.min-score:0.75}") Double minScore) {
        this.host = host;
        this.port = port;
        this.collectionName = collectionName;
        this.maxResults = maxResults;
        this.minScore = minScore;
    }

    /**
     * Creates the local BGE-small-en quantized embedding model bean.
     *
     * <p>This model produces 384-dimensional vector embeddings from text input.
     * It runs entirely on the local JVM without external API calls, providing
     * low-latency embedding computation for both ingestion and query-time
     * vector operations.</p>
     *
     * @return the embedding model instance
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        log.debug("[RAG] Creating local BGE-small-en embedding model (384-dim)");
        return new BgeSmallEnV15QuantizedEmbeddingModel();
    }

    /**
     * Creates the Qdrant gRPC client for vector store communication.
     *
     * <p>Establishes a non-TLS gRPC connection to the Qdrant server using the
     * configured host and port. This client is shared across the embedding store
     * and collection management operations.</p>
     *
     * @return the Qdrant client instance
     */
    @Bean
    public QdrantClient qdrantClient() {
        return new QdrantClient(
                QdrantGrpcClient.newBuilder(host, port, false).build()
        );
    }

    /**
     * Creates the Qdrant-backed embedding store bean.
     *
     * <p>This method ensures that the target Qdrant collection exists before
     * building the store. If the collection is missing, it is created with
     * the embedding model's dimensionality and cosine distance metric. The
     * resulting store is used for both vector ingestion (product catalog
     * indexing) and retrieval (semantic search during conversations).</p>
     *
     * @param qdrantClient   the Qdrant gRPC client
     * @param embeddingModel the local embedding model
     * @return the embedding store instance
     */
    @Bean
    public EmbeddingStore<dev.langchain4j.data.segment.TextSegment> embeddingStore(
            QdrantClient qdrantClient,
            EmbeddingModel embeddingModel) {

        int dimension = embeddingModel.dimension();
        ensureCollectionExists(qdrantClient, dimension);

        log.debug("[RAG] Building QdrantEmbeddingStore (collection={}, dim={})", collectionName, dimension);
        return QdrantEmbeddingStore.builder()
                .client(qdrantClient)
                .collectionName(collectionName)
                .build();
    }

    /**
     * Creates the content retriever used by the AI model for product-aware responses.
     *
     * <p>The retriever is configured with an in-stock filter that ensures only
     * currently available products are returned during semantic search. It uses
     * the configured maximum results and minimum similarity score thresholds
     * to balance relevance with response quality.</p>
     *
     * @param embeddingStore the vector store containing product embeddings
     * @param embeddingModel the embedding model for query-time vectorization
     * @return the content retriever instance
     */
    @Bean
    public ContentRetriever contentRetriever(
            EmbeddingStore<dev.langchain4j.data.segment.TextSegment> embeddingStore,
            EmbeddingModel embeddingModel) {

        // Only surface in-stock products to the assistant by default.
        // NOTE: inStock is stored as a String ("true"/"false") in the payload,
        // so the filter value must be a String to match.
        Filter filter = new IsEqualTo("inStock", "true");

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .filter(filter)
                .build();
    }

    /**
     * Ensures that the target Qdrant collection exists, creating it if necessary.
     *
     * <p>Checks the Qdrant server for the configured collection name. If the
     * collection does not exist, it is created with the specified vector
     * dimensionality and cosine distance metric. Throws an
     * {@link IllegalStateException} if the collection cannot be created,
     * preventing the application from starting with a non-functional RAG
     * pipeline.</p>
     *
     * @param client    the Qdrant client
     * @param dimension the embedding vector dimensionality
     */
    private void ensureCollectionExists(QdrantClient client, int dimension) {
        try {
            if (client.listCollectionsAsync().get().contains(collectionName)) {
                log.debug("[RAG] Qdrant collection '{}' already exists", collectionName);
                return;
            }
            Collections.VectorParams vectorParams = Collections.VectorParams.newBuilder()
                    .setSize(dimension)
                    .setDistance(Collections.Distance.Cosine)
                    .build();
            client.createCollectionAsync(collectionName, vectorParams).get();
            log.info("[RAG] Created Qdrant collection '{}' (dim={}, distance=COSINE)", collectionName, dimension);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to initialise Qdrant collection '" + collectionName + "'", e);
        }
    }
}
