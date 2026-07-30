package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.admin.response.AdminProductResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.service.admin.AdminProductService;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Admin REST controller for product lifecycle management.
 *
 * <p>Handles the approval workflow for seller-submitted products. Products are
 * created in DRAFT status by sellers and must be reviewed by an admin before
 * appearing on the storefront. The admin can publish (approve) or reject each
 * pending product submission.</p>
 *
 * <p>This controller acts as the HTTP boundary for the product approval pipeline.
 * It delegates all business logic — status transitions, storefront visibility
 * toggling, and notification dispatch — to {@link AdminProductService}. The
 * controller handles no business logic beyond page-size capping.</p>
 *
 * <p>All endpoints require ADMIN role and are mounted under /api/admin/products.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * GET  /api/admin/products/pending       — list DRAFT products pending review
 * PUT  /api/admin/products/{id}/publish  — publish a product to the storefront
 * PUT  /api/admin/products/{id}/reject   — reject a pending product
 * </pre>
 *
 * @author prabhatkrmishra
 * @see AdminProductService
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final AdminProductService adminProductService;
    private final PaginationProperties pagination;

    public AdminProductController(AdminProductService adminProductService, PaginationProperties pagination) {
        this.adminProductService = adminProductService;
        this.pagination = pagination;
    }

    /**
     * Returns a paginated list of products in DRAFT status pending admin review.
     *
     * <p>Only products with a status of DRAFT (submitted by sellers) are
     * included. The page size is capped by the configured maximum.</p>
     *
     * @param page zero-based page index
     * @param size page size (capped by the configured maximum)
     * @return 200 OK with page of pending product responses
     */
    @GetMapping("/pending")
    public ResponseEntity<PageResponse<AdminProductResponse>> getPendingProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return ResponseEntity.ok(adminProductService.getPendingProducts(page, size));
    }

    /**
     * Publishes a pending product, making it visible on the storefront.
     *
     * <p>Transitions the product from DRAFT to PUBLISHED status. The product
     * will immediately become browsable in the catalog.</p>
     *
     * @param id the product ID to publish
     * @return 200 OK with the updated product response
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<AdminProductResponse> publishProduct(
            @PathVariable Long id) {
        return ResponseEntity.ok(adminProductService.publishProduct(id));
    }

    /**
     * Rejects a pending product, preventing it from being published.
     *
     * <p>The product status is set to REJECTED. The seller can review the
     * rejection and resubmit after making corrections.</p>
     *
     * @param id the product ID to reject
     * @return 200 OK with the updated product response
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<AdminProductResponse> rejectProduct(
            @PathVariable Long id) {
        return ResponseEntity.ok(adminProductService.rejectProduct(id));
    }
}
