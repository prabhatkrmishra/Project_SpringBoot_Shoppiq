package com.pkmprojects.shoppiq.controller.seller;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerInventoryResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.service.seller.SellerInventoryService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for seller inventory management.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>List the seller's full inventory with stock status.</li>
 *     <li>Identify low stock products.</li>
 *     <li>Identify out of stock products.</li>
 *     <li>Adjust stock quantities for individual products.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>All endpoints require SELLER or ADMIN role.</li>
 *     <li>The authenticated user is injected via {@link AuthenticationPrincipal}.</li>
 *     <li>Ownership is enforced at the service layer.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/seller/inventory")
public class SellerInventoryController {

    private final SellerInventoryService sellerInventoryService;
    private final PaginationProperties pagination;

    public SellerInventoryController(SellerInventoryService sellerInventoryService, PaginationProperties pagination) {
        this.sellerInventoryService = sellerInventoryService;
        this.pagination = pagination;
    }

    @GetMapping
    public ResponseEntity<PageResponse<SellerInventoryResponse>> getInventory(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return ResponseEntity.ok(sellerInventoryService.getInventory(currentUser, page, size));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<PageResponse<SellerInventoryResponse>> getLowStock(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return ResponseEntity.ok(sellerInventoryService.getLowStockProducts(currentUser, page, size));
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<PageResponse<SellerInventoryResponse>> getOutOfStock(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return ResponseEntity.ok(sellerInventoryService.getOutOfStockProducts(currentUser, page, size));
    }

    @PutMapping("/{id}/adjust")
    public ResponseEntity<SellerInventoryResponse> adjustStock(
            @PathVariable @Positive(message = "Product id must be a positive number") Long id,
            @RequestParam @NotNull(message = "Quantity is required.") int quantity,
            @RequestParam @NotBlank(message = "Reason is required.")
            @Size(max = 255, message = "Reason cannot exceed 255 characters.") String reason,
            @AuthenticationPrincipal User currentUser) {
        SellerInventoryResponse response = sellerInventoryService.adjustStock(id, quantity, reason, currentUser);
        return ResponseEntity.ok(response);
    }
}
