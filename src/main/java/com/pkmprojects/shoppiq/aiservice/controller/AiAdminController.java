package com.pkmprojects.shoppiq.aiservice.controller;

import com.pkmprojects.shoppiq.aiservice.exception.AiServiceUnavailableException;
import com.pkmprojects.shoppiq.aiservice.ingestion.ProductCatalogIngester;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Administrative REST controller for managing the RAG product vector store.
 *
 * <p>This controller provides administrative endpoints for managing the
 * Qdrant vector store used by the AI chat service's Retrieval-Augmented
 * Generation pipeline. It currently supports triggering a full product
 * catalog reindex, which clears all existing embeddings and rebuilds them
 * from the current catalog data.</p>
 *
 * <p>All endpoints require {@code ROLE_ADMIN} security clearance and are
 * conditionally enabled via the {@code shoppiq.ai.enabled} property.
 * The reindex operation is synchronous and may take several minutes for
 * large catalogs.</p>
 *
 * <ul>
 *     <li>{@code POST /api/ai/admin/reindex} — trigger a full product catalog reindex</li>
 * </ul>
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

    private final ProductCatalogIngester productCatalogIngester;

    public AiAdminController(@Nullable ProductCatalogIngester productCatalogIngester) {
        this.productCatalogIngester = productCatalogIngester;
    }

    @PostConstruct
    void logInit() {
        log.debug("[RAG] AiAdminController registered — ingesterAvailable={}", productCatalogIngester != null);
    }

    private void checkServiceAvailable() {
        if (productCatalogIngester == null) {
            throw AiServiceUnavailableException.disabled();
        }
    }

    /**
     * Triggers a full reindex of the product catalog into the Qdrant vector store.
     *
     * <p>Removes all existing embeddings and rebuilds them from the current
     * product catalog. This operation is synchronous and may take several
     * minutes for large catalogs. Requires {@code ROLE_ADMIN} and the
     * {@code shoppiq.ai.enabled} property to be set to {@code true}.</p>
     *
     * @return 200 OK with {@code {"status": "reindexed"}} on success
     */
    @PostMapping("/reindex")
    public ResponseEntity<?> reindex() {
        checkServiceAvailable();
        productCatalogIngester.reindexAll();
        return ResponseEntity.ok(Map.of("status", "reindexed"));
    }
}
