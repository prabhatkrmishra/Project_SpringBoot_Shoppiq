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
 * @author PrabhatKrMishra
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

    @PutMapping("/{sellerId}/approve")
    public ResponseEntity<AdminSellerResponse> approveSeller(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(adminSellerService.approveSeller(sellerId));
    }

    @PutMapping("/{sellerId}/reject")
    public ResponseEntity<AdminSellerResponse> rejectSeller(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(adminSellerService.rejectSeller(sellerId));
    }

    @PutMapping("/{sellerId}/suspend")
    public ResponseEntity<AdminSellerResponse> suspendSeller(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(adminSellerService.suspendSeller(sellerId));
    }

    @PutMapping("/{sellerId}/unsuspend")
    public ResponseEntity<AdminSellerResponse> unsuspendSeller(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(adminSellerService.unsuspendSeller(sellerId));
    }
}
