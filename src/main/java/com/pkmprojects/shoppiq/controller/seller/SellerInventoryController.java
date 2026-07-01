package com.pkmprojects.shoppiq.controller.seller;

import com.pkmprojects.shoppiq.dto.seller.response.SellerInventoryResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.service.seller.SellerInventoryService;
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

import java.util.List;

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

    public SellerInventoryController(SellerInventoryService sellerInventoryService) {
        this.sellerInventoryService = sellerInventoryService;
    }

    @GetMapping
    public ResponseEntity<List<SellerInventoryResponse>> getInventory(
            @AuthenticationPrincipal User currentUser) {
        List<SellerInventoryResponse> inventory = sellerInventoryService.getInventory(currentUser);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<SellerInventoryResponse>> getLowStock(
            @AuthenticationPrincipal User currentUser) {
        List<SellerInventoryResponse> lowStock = sellerInventoryService.getLowStockProducts(currentUser);
        return ResponseEntity.ok(lowStock);
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<SellerInventoryResponse>> getOutOfStock(
            @AuthenticationPrincipal User currentUser) {
        List<SellerInventoryResponse> outOfStock = sellerInventoryService.getOutOfStockProducts(currentUser);
        return ResponseEntity.ok(outOfStock);
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
