package com.pkmprojects.shoppiq.service.checkout;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.order.*;
import com.pkmprojects.shoppiq.entity.user.User;

/**
 * Business contract for checkout and order operations.
 *
 * <p>Defines operations for the full checkout flow, order cost previews,
 * order history, and order lifecycle actions (cancel, return, refund, replacement).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface CheckoutService {

    /**
     * Places a new order from the authenticated user's cart.
     *
     * @param user    the authenticated customer
     * @param request checkout details
     * @return the created order
     */
    CheckoutResponse checkout(User user, CheckoutRequest request);

    /**
     * Calculates the full order cost breakdown from the user's cart
     * without placing an order.
     *
     * @param user    authenticated customer
     * @param request payment and delivery selections
     * @return full cost breakdown
     */
    OrderCalculationResponse calculateOrderSummary(User user, OrderCalculationRequest request);

    /**
     * Returns all orders belonging to the authenticated user (paginated).
     *
     * @param user authenticated customer
     * @param page zero-based page index
     * @param size page size
     * @return paginated order responses
     */
    PageResponse<OrderResponse> getMyOrders(User user, int page, int size);

    /**
     * Returns a single order by id.
     *
     * @param user    authenticated customer
     * @param orderId target order id
     * @return full order response
     */
    OrderResponse getMyOrder(User user, Long orderId);

    /**
     * Requests cancellation for an order in {@code PLACED} status.
     *
     * @param user    authenticated customer
     * @param orderId target order id
     */
    void cancelOrder(User user, Long orderId);

    /**
     * Requests a return for an order in {@code DELIVERED} status.
     *
     * @param user    authenticated customer
     * @param orderId target order id
     */
    void requestReturn(User user, Long orderId);

    /**
     * Requests a refund for an order in {@code DELIVERED} status.
     *
     * @param user    authenticated customer
     * @param orderId target order id
     */
    void requestRefund(User user, Long orderId);

    /**
     * Requests a replacement for an order in {@code DELIVERED} status.
     *
     * @param user    authenticated customer
     * @param orderId target order id
     */
    void requestReplacement(User user, Long orderId);
}
