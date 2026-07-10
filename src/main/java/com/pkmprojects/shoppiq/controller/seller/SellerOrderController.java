package com.pkmprojects.shoppiq.controller.seller;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.service.seller.SellerOrderService;
import jakarta.validation.constraints.Min;
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
    private final PaginationProperties pagination;

    public SellerOrderController(SellerOrderService sellerOrderService, PaginationProperties pagination) {
        this.sellerOrderService = sellerOrderService;
        this.pagination = pagination;
    }

    @GetMapping
    public ResponseEntity<PageResponse<SellerOrderResponse>> getOrders(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        PageResponse<SellerOrderResponse> orders = sellerOrderService.getOrders(currentUser, page, size);
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
