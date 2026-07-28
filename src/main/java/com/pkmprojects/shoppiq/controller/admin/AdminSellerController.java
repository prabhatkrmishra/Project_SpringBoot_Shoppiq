package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.admin.response.AdminSellerResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.service.admin.AdminSellerService;
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
 * Admin REST controller for seller management.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *     <li>GET  /api/admin/sellers                — list all sellers</li>
 *     <li>GET  /api/admin/sellers?status=PENDING  — filter by verification status</li>
 *     <li>PUT  /api/admin/sellers/{id}/approve    — approve seller</li>
 *     <li>PUT  /api/admin/sellers/{id}/reject     — reject seller</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/api/admin/sellers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSellerController {

    private final AdminSellerService adminSellerService;
    private final PaginationProperties pagination;

    public AdminSellerController(AdminSellerService adminSellerService, PaginationProperties pagination) {
        this.adminSellerService = adminSellerService;
        this.pagination = pagination;
    }

    /**
     * Returns a paginated list of sellers, optionally filtered by verification status.
     *
     * @param status optional verification status filter (PENDING, APPROVED, REJECTED)
     * @param page   zero-based page index
     * @param size   page size (capped by {@code pagination.maxPageSize()})
     * @return 200 OK with page of seller responses
     */
    @GetMapping
    public ResponseEntity<PageResponse<AdminSellerResponse>> getSellers(
            @RequestParam(required = false) VerificationStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        if (status != null) {
            return ResponseEntity.ok(adminSellerService.getSellersByStatus(status, page, size));
        }
        return ResponseEntity.ok(adminSellerService.getAllSellers(page, size));
    }

    /**
     * Approves a seller's registration, enabling them to list products.
     *
     * @param sellerId the seller ID
     * @return 200 OK with the updated seller response
     */
    @PutMapping("/{sellerId}/approve")
    public ResponseEntity<AdminSellerResponse> approveSeller(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(adminSellerService.approveSeller(sellerId));
    }

    /**
     * Rejects a seller's registration application.
     *
     * @param sellerId the seller ID
     * @return 200 OK with the updated seller response
     */
    @PutMapping("/{sellerId}/reject")
    public ResponseEntity<AdminSellerResponse> rejectSeller(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(adminSellerService.rejectSeller(sellerId));
    }

    /**
     * Suspends an approved seller, temporarily disabling their storefront.
     *
     * @param sellerId the seller ID
     * @return 200 OK with the updated seller response
     */
    @PutMapping("/{sellerId}/suspend")
    public ResponseEntity<AdminSellerResponse> suspendSeller(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(adminSellerService.suspendSeller(sellerId));
    }

    /**
     * Unsuspends a previously suspended seller, reactivating their storefront.
     *
     * @param sellerId the seller ID
     * @return 200 OK with the updated seller response
     */
    @PutMapping("/{sellerId}/unsuspend")
    public ResponseEntity<AdminSellerResponse> unsuspendSeller(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(adminSellerService.unsuspendSeller(sellerId));
    }
}
