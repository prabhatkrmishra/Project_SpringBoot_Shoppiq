package com.pkmprojects.shoppiq.controller.seller;

import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.service.seller.SellerOrderService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
 * REST controller for seller order management.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>List orders containing the authenticated seller's products.</li>
 *     <li>Retrieve a specific order filtered to the seller's line items.</li>
 *     <li>Update order status when all items belong to the seller.</li>
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
@RequestMapping("/seller/orders")
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    public SellerOrderController(SellerOrderService sellerOrderService) {
        this.sellerOrderService = sellerOrderService;
    }

    @GetMapping
    public ResponseEntity<List<SellerOrderResponse>> getOrders(
            @AuthenticationPrincipal User currentUser) {
        List<SellerOrderResponse> orders = sellerOrderService.getOrders(currentUser);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SellerOrderResponse> getOrder(
            @PathVariable @Positive(message = "Order id must be a positive number") Long id,
            @AuthenticationPrincipal User currentUser) {
        SellerOrderResponse response = sellerOrderService.getOrder(currentUser, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<SellerOrderResponse> updateOrderStatus(
            @PathVariable @Positive(message = "Order id must be a positive number") Long id,
            @RequestParam @NotNull(message = "Status is required.") OrderStatus status,
            @AuthenticationPrincipal User currentUser) {
        SellerOrderResponse response = sellerOrderService.updateOrderStatus(currentUser, id, status);
        return ResponseEntity.ok(response);
    }
}
