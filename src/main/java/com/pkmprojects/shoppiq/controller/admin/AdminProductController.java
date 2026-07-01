package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminProductResponse;
import com.pkmprojects.shoppiq.service.admin.AdminProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin REST controller for product lifecycle management.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *     <li>GET  /api/admin/products/pending    — list DRAFT products</li>
 *     <li>PUT  /api/admin/products/{id}/publish — publish a product</li>
 *     <li>PUT  /api/admin/products/{id}/reject  — reject a product</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<AdminProductResponse>> getPendingProducts() {
        return ResponseEntity.ok(adminProductService.getPendingProducts());
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<AdminProductResponse> publishProduct(
            @PathVariable Long id) {
        return ResponseEntity.ok(adminProductService.publishProduct(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<AdminProductResponse> rejectProduct(
            @PathVariable Long id) {
        return ResponseEntity.ok(adminProductService.rejectProduct(id));
    }
}
