package com.pkmprojects.shoppiq.controller.order;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.order.*;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.service.checkout.CheckoutService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing order-management endpoints for authenticated customers.
 *
 * <p>Provides checkout, order listing, order detail, cancellation, return, refund,
 * and replacement operations. The checkout endpoint converts the user's cart into
 * an order, while the lifecycle endpoints allow customers to request status changes
 * on existing orders. All operations resolve the authenticated user from the
 * Spring Security context and enforce ownership at the service layer.</p>
 *
 * <p>This controller acts as the HTTP boundary for customer order operations. It
 * delegates all business logic — cart-to-order conversion, cost calculation,
 * status transitions, ownership validation, and order querying — to
 * {@link CheckoutService}. The controller handles no business logic beyond
 * page-size capping and request validation.</p>
 *
 * <p>All endpoints are scoped to /user/order and require authentication. The
 * authenticated user is resolved from AuthenticationPrincipal and is never
 * accepted from client-supplied data.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * POST   /user/order/checkout       — place an order from the cart
 * POST   /user/order/calculate      — calculate order cost without placing
 * GET    /user/order                — list all orders (paginated)
 * GET    /user/order/{id}           — get a single order by ID
 * PUT    /user/order/cancel/{id}    — request cancellation (PLACED only)
 * PUT    /user/order/return/{id}    — request return (DELIVERED only)
 * PUT    /user/order/refund/{id}    — request refund (DELIVERED only)
 * PUT    /user/order/replace/{id}   — request replacement (DELIVERED only)
 * </pre>
 *
 * @author prabhatkrmishra
 * @see CheckoutService
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/user/order")
@RequiredArgsConstructor
public class OrderController {

    private final CheckoutService checkoutService;
    private final PaginationProperties pagination;

    // =========================================================
    // Checkout
    // =========================================================

    /**
     * Places a new order from the authenticated user's cart.
     *
     * <p>Converts the user's cart into an order, validates stock availability,
     * calculates totals, and returns a lightweight checkout confirmation.</p>
     *
     * @param user    the authenticated customer
     * @param request the checkout payload (addressId, paymentMethod)
     * @return 201 Created with the checkout response
     */
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody CheckoutRequest request) {

        CheckoutResponse response = checkoutService.checkout(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Calculates the full order cost breakdown from the user's cart without
     * placing an order.
     *
     * <p>Call this from the payment page whenever the payment method or
     * delivery type changes so the frontend always displays server-calculated
     * values.</p>
     *
     * @param user    the authenticated customer
     * @param request the payment and delivery selections
     * @return 200 OK with cost breakdown response
     */
    @PostMapping("/calculate")
    public ResponseEntity<OrderCalculationResponse> calculateOrderSummary(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody OrderCalculationRequest request) {

        OrderCalculationResponse response = checkoutService.calculateOrderSummary(user, request);
        return ResponseEntity.ok(response);
    }

    // =========================================================
    // Query
    // =========================================================

    /**
     * Returns all orders belonging to the authenticated user.
     *
     * @param user the authenticated customer
     * @return 200 OK with page of order responses
     */
    @GetMapping("")
    public ResponseEntity<PageResponse<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal(expression = "user") User user,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return ResponseEntity.ok(checkoutService.getMyOrders(user, page, size));
    }

    /**
     * Returns a single order by ID, scoped to the authenticated user.
     *
     * @param user    the authenticated customer
     * @param orderId the order ID to retrieve (must be positive)
     * @return 200 OK with the full order response
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getMyOrder(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable("id") @Positive(message = "Order id must be a positive number.") Long orderId) {

        return ResponseEntity.ok(checkoutService.getMyOrder(user, orderId));
    }

    // =========================================================
    // Cancellation
    // =========================================================

    /**
     * Requests cancellation for an order in PLACED status.
     *
     * <p>Only orders with PLACED status can be cancelled. Once the order
     * has been shipped or delivered, cancellation is no longer available.</p>
     *
     * @param user    the authenticated customer
     * @param orderId the order ID to cancel (must be positive)
     * @return 204 No Content
     */
    @PutMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelOrder(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable("id") @Positive(message = "Order id must be a positive number.") Long orderId) {

        checkoutService.cancelOrder(user, orderId);
        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // Return
    // =========================================================

    /**
     * Requests a return for an order in DELIVERED status.
     *
     * <p>Only orders with DELIVERED status can be returned. The return
     * request may require admin approval depending on business rules.</p>
     *
     * @param user    the authenticated customer
     * @param orderId the order ID to return (must be positive)
     * @return 204 No Content
     */
    @PutMapping("/return/{id}")
    public ResponseEntity<Void> requestReturn(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable("id") @Positive(message = "Order id must be a positive number.") Long orderId) {

        checkoutService.requestReturn(user, orderId);
        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // Refund
    // =========================================================

    /**
     * Requests a refund for an order in DELIVERED status.
     *
     * <p>Only orders with DELIVERED status can be refunded. The refund
     * request may require admin approval depending on business rules.</p>
     *
     * @param user    the authenticated customer
     * @param orderId the order ID to refund (must be positive)
     * @return 204 No Content
     */
    @PutMapping("/refund/{id}")
    public ResponseEntity<Void> requestRefund(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable("id") @Positive(message = "Order id must be a positive number.") Long orderId) {

        checkoutService.requestRefund(user, orderId);
        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // Replacement
    // =========================================================

    /**
     * Requests a replacement for an order in DELIVERED status.
     *
     * <p>Only orders with DELIVERED status can be replaced. The replacement
     * request may require admin approval depending on business rules.</p>
     *
     * @param user    the authenticated customer
     * @param orderId the order ID to replace (must be positive)
     * @return 204 No Content
     */
    @PutMapping("/replace/{id}")
    public ResponseEntity<Void> requestReplacement(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable("id") @Positive(message = "Order id must be a positive number.") Long orderId) {

        checkoutService.requestReplacement(user, orderId);
        return ResponseEntity.noContent().build();
    }
}
