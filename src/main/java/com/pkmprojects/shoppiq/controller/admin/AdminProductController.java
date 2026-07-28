package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.admin.response.AdminProductResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.service.admin.AdminProductService;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <strong>Spring Boot Concept:</strong> Admin REST controller for product lifecycle management.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *     <li>GET  /api/admin/products/pending    — list DRAFT products</li>
 *     <li>PUT  /api/admin/products/{id}/publish — publish a product</li>
 *     <li>PUT  /api/admin/products/{id}/reject  — reject a product</li>
 * </ul>
 *
 * @author prabhatkrmishra
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
     * @param page zero-based page index
     * @param size page size (capped by {@code pagination.maxPageSize()})
     * @return 200 OK with page of pending products
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
     * @param id the product ID
     * @return 200 OK with the updated product
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<AdminProductResponse> publishProduct(
            @PathVariable Long id) {
        return ResponseEntity.ok(adminProductService.publishProduct(id));
    }

    /**
     * Rejects a pending product, preventing it from being published.
     *
     * @param id the product ID
     * @return 200 OK with the updated product (status remains DRAFT or set to REJECTED)
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<AdminProductResponse> rejectProduct(
            @PathVariable Long id) {
        return ResponseEntity.ok(adminProductService.rejectProduct(id));
    }
}
