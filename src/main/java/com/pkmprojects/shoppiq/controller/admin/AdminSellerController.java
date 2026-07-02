package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminSellerResponse;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.service.admin.AdminSellerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
@RestController
@RequestMapping("/api/admin/sellers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSellerController {

    private final AdminSellerService adminSellerService;

    public AdminSellerController(AdminSellerService adminSellerService) {
        this.adminSellerService = adminSellerService;
    }

    @GetMapping
    public ResponseEntity<List<AdminSellerResponse>> getSellers(
            @RequestParam(required = false) VerificationStatus status) {
        if (status != null) {
            return ResponseEntity.ok(adminSellerService.getSellersByStatus(status));
        }
        return ResponseEntity.ok(adminSellerService.getAllSellers());
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
