package com.pkmprojects.shoppiq.aiservice.controller;

import com.pkmprojects.shoppiq.aiservice.ingestion.ProductCatalogIngester;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Administrative endpoints for managing the RAG product vector store.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/admin")
@PreAuthorize("hasRole('ADMIN')")
@ConditionalOnProperty(name = "shoppiq.ai.enabled", havingValue = "true", matchIfMissing = false)
public class AiAdminController {

    @Autowired(required = false)
    private ProductCatalogIngester productCatalogIngester;

    @PostConstruct
    void logInit() {
        log.debug("[RAG] AiAdminController registered — ingesterAvailable={}", productCatalogIngester != null);
    }

    /**
     * Triggers a full reindex of the product catalog into the Qdrant vector store.
     *
     * <p>
     * Removes all existing embeddings and rebuilds them from the current catalog.
     * Requires {@code ROLE_ADMIN} and the {@code ai-enabled} profile.
     *
     * @return 200 OK with {@code {"status": "reindexed"}} on success
     */
    @PostMapping("/reindex")
    public ResponseEntity<?> reindex() {
        if (productCatalogIngester == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "RAG ingester is not available. Check ai-enabled profile and Qdrant."));
        }
        productCatalogIngester.reindexAll();
        return ResponseEntity.ok(Map.of("status", "reindexed"));
    }
}
