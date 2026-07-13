package com.pkmprojects.shoppiq.aiservice.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15.BgeSmallEnV15EmbeddingModel;
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
 * Configuration for Retrieval-Augmented Generation (RAG).
 *
 * <p>
 * Wires together the local embedding model (BGE-small-en, 384-dim), the Qdrant
 * vector store (running as a docker-compose service), and a {@link ContentRetriever}
 * that injects the top-k most relevant product chunks into the LLM prompt before
 * each response.
 *
 * <p>
 * The {@link QdrantEmbeddingStore} from LangChain4j does <em>not</em> auto-create
 * its collection, so this config explicitly creates the collection (cosine
 * distance, dimension matching the embedding model) on startup if it is absent.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "shoppiq.ai.enabled", havingValue = "true", matchIfMissing = false)
public class RagConfig {

    private static final Logger log = LoggerFactory.getLogger(RagConfig.class);

    @Value("${langchain4j.qdrant.host:localhost}")
    private String host;

    @Value("${langchain4j.qdrant.port:6334}")
    private int port;

    @Value("${langchain4j.qdrant.collection-name:shoppiq_products}")
    private String collectionName;

    @Value("${shoppiq.ai.rag.max-results:5}")
    private Integer maxResults;

    @Value("${shoppiq.ai.rag.min-score:0.75}")
    private Double minScore;

    @Bean
    public EmbeddingModel embeddingModel() {
        log.debug("[RAG] Creating local BGE-small-en embedding model (384-dim)");
        return new BgeSmallEnV15EmbeddingModel();
    }

    @Bean
    public QdrantClient qdrantClient() {
        return new QdrantClient(
                QdrantGrpcClient.newBuilder(host, port, false).build()
        );
    }

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
