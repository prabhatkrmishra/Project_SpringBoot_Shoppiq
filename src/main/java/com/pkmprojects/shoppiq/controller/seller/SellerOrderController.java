package com.pkmprojects.shoppiq.controller.seller;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.service.seller.SellerOrderService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for seller order management.
 *
 * <p>Exposes endpoints for sellers to list, view, and update the status of orders
 * containing their products. Order data is filtered to show only the seller's own
 * line items. Sellers can update order status (e.g., mark as shipped) when all
 * items in the order belong to them.</p>
 *
 * <p>This controller acts as the HTTP boundary for seller order operations. It
 * delegates all business logic — order retrieval, line-item filtering, status
 * transitions, and ownership validation — to {@link SellerOrderService}. The
 * controller handles no business logic beyond page-size capping.</p>
 *
 * <p>All endpoints require SELLER or ADMIN role and are mounted under
 * /seller/orders.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * GET    /seller/orders          — paginated list of orders with seller products
 * GET    /seller/orders/{id}     — single order detail (filtered to seller items)
 * PUT    /seller/orders/{id}/status — update order status
 * </pre>
 *
 * @author prabhatkrmishra
 * @see SellerOrderService
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

    /**
     * Returns a paginated list of orders containing the authenticated seller's
     * products.
     *
     * @param currentUser the authenticated seller
     * @param page        zero-based page index
     * @param size        page size (capped by the configured maximum)
     * @return 200 OK with page of order responses
     */
    @GetMapping
    public ResponseEntity<PageResponse<SellerOrderResponse>> getOrders(
            @AuthenticationPrincipal(expression = "user") User currentUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        PageResponse<SellerOrderResponse> orders = sellerOrderService.getOrders(currentUser, page, size);
        return ResponseEntity.ok(orders);
    }

    /**
     * Returns a single order by ID, filtered to the seller's line items.
     *
     * @param id          the order ID (must be positive)
     * @param currentUser the authenticated seller
     * @return 200 OK with the filtered order response
     */
    @GetMapping("/{id}")
    public ResponseEntity<SellerOrderResponse> getOrder(
            @PathVariable @Positive(message = "Order id must be a positive number") Long id,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        SellerOrderResponse response = sellerOrderService.getOrder(currentUser, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the status of an order when all items belong to the seller.
     *
     * <p>Validates that the seller owns all items in the order before
     * allowing the status transition.</p>
     *
     * @param id          the order ID (must be positive)
     * @param status      the new order status
     * @param currentUser the authenticated seller
     * @return 200 OK with the updated order response
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<SellerOrderResponse> updateOrderStatus(
            @PathVariable @Positive(message = "Order id must be a positive number") Long id,
            @RequestParam @NotNull(message = "Status is required.") OrderStatus status,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        SellerOrderResponse response = sellerOrderService.updateOrderStatus(currentUser, id, status);
        return ResponseEntity.ok(response);
    }
}