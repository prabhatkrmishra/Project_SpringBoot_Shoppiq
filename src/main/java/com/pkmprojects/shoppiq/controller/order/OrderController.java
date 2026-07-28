package com.pkmprojects.shoppiq.controller.order;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.order.CheckoutRequest;
import com.pkmprojects.shoppiq.dto.order.CheckoutResponse;
import com.pkmprojects.shoppiq.dto.order.OrderCalculationRequest;
import com.pkmprojects.shoppiq.dto.order.OrderCalculationResponse;
import com.pkmprojects.shoppiq.dto.order.OrderResponse;
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
 * <strong>Spring Boot Concept:</strong> REST controller exposing order-management endpoints for authenticated customers.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code POST /user/order/checkout}       — place an order</li>
 *   <li>{@code GET  /user/order/get/all}        — list all orders</li>
 *   <li>{@code GET  /user/order/get/{id}}       — get a single order</li>
 *   <li>{@code PUT  /user/order/cancel/{id}}    — request cancellation (PLACED only)</li>
 *   <li>{@code PUT  /user/order/return/{id}}    — request return (DELIVERED only)</li>
 *   <li>{@code PUT  /user/order/refund/{id}}    — request refund (DELIVERED only)</li>
 *   <li>{@code PUT  /user/order/replace/{id}}   — request replacement (DELIVERED only)</li>
 * </ul>
 *
 * <p>
 * All operations resolve the authenticated user from Spring Security context.
 * Ownership is enforced at the service layer.
 * </p>
 *
 * @author prabhatkrmishra
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
     * @param user    authenticated customer
     * @param request checkout payload (addressId, paymentMethod)
     * @return 201 Created with lightweight checkout response
     */
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody CheckoutRequest request) {

        CheckoutResponse response = checkoutService.checkout(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Calculates the full order cost breakdown from the user's cart
     * without placing an order.
     *
     * <p>Call this from the payment page whenever the payment method or
     * delivery type changes so the frontend always displays server-calculated
     * values.</p>
     *
     * @param user    authenticated customer
     * @param request payment and delivery selections
     * @return 200 OK with cost breakdown
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
     * @param user authenticated customer
     * @return 200 OK with list of order responses
     */
    @GetMapping("/get/all")
    public ResponseEntity<PageResponse<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal(expression = "user") User user,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return ResponseEntity.ok(checkoutService.getMyOrders(user, page, size));
    }

    /**
     * Returns a single order by id.
     *
     * @param user    authenticated customer
     * @param orderId order id (must be positive)
     * @return 200 OK with full order response
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<OrderResponse> getMyOrder(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable("id") @Positive(message = "Order id must be a positive number.") Long orderId) {

        return ResponseEntity.ok(checkoutService.getMyOrder(user, orderId));
    }

    // =========================================================
    // Cancellation
    // =========================================================

    /**
     * Requests cancellation for an order in {@code PLACED} status.
     *
     * @param user    authenticated customer
     * @param orderId order id (must be positive)
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
     * Requests a return for an order in {@code DELIVERED} status.
     *
     * @param user    authenticated customer
     * @param orderId order id (must be positive)
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
     * Requests a refund for an order in {@code DELIVERED} status.
     *
     * @param user    authenticated customer
     * @param orderId order id (must be positive)
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
     * Requests a replacement for an order in {@code DELIVERED} status.
     *
     * @param user    authenticated customer
     * @param orderId order id (must be positive)
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
