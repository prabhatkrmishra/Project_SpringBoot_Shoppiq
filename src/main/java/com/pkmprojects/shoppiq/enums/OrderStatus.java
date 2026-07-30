package com.pkmprojects.shoppiq.enums;

import com.pkmprojects.shoppiq.entity.order.Order;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Lifecycle states of an {@link Order} defining valid status transitions.
 *
 * <p>This enum models the complete order lifecycle from placement through
 * delivery, including cancellation, return, refund, and replacement
 * workflows. Each status has a defined set of valid next states, enforced
 * by the {@link #canTransitionTo(OrderStatus)} method. Invalid transitions
 * are rejected with an {@link com.pkmprojects.shoppiq.exception.general.order.OrderInvalidStatusTransitionException}.</p>
 *
 * <p>The transition map is defined as a static {@link EnumMap} that is
 * populated in a static initializer block. This design ensures that the
 * valid transitions are evaluated in constant time and that the
 * transition rules are centralized in a single location rather than
 * scattered across service methods.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum OrderStatus {

    /**
     * Initial state after a customer places an order.
     *
     * <p>The order has been created and payment has been initiated.
     * From this state, the order can be confirmed by a seller/admin,
     * cancelled by the customer, or cancelled directly.</p>
     */
    PLACED,
    /**
     * Seller or admin has confirmed the order for processing.
     *
     * <p>The order is being prepared for shipment. From this state,
     * the next expected transition is to SHIPPED when the package
     * is handed off to a carrier.</p>
     */
    CONFIRMED,
    /**
     * Order has been handed off to a shipping carrier.
     *
     * <p>The package is in transit. From this state, the next expected
     * transition is to OUT_FOR_DELIVERY when the carrier begins
     * final-mile delivery.</p>
     */
    SHIPPED,
    /**
     * Order is out for final-mile delivery.
     *
     * <p>The package is on the delivery vehicle and will be delivered
     * today. From this state, the next expected transition is to
     * DELIVERED when the customer receives the package.</p>
     */
    OUT_FOR_DELIVERY,
    /**
     * Customer has received the order.
     *
     * <p>This is the terminal state for successful deliveries. From
     * this state, the customer can request a return, refund, or
     * replacement within the allowed window.</p>
     */
    DELIVERED,
    /**
     * Order was cancelled (directly or after a cancel request).
     *
     * <p>This is a terminal state. The order cannot be further
     * modified. Cancellation may have been initiated by the customer,
     * seller, or admin.</p>
     */
    CANCELLED,
    /**
     * Return process completed; item has been returned.
     *
     * <p>This is a terminal state for returned orders. The item has
     * been received back and inspected. A refund or replacement may
     * follow depending on the return reason.</p>
     */
    RETURNED,
    /**
     * Customer has requested cancellation; awaiting approval.
     *
     * <p>The customer wants to cancel the order before it ships.
     * From this state, the request can be approved (transition to
     * CANCELLED) or denied (remaining in CANCEL_REQUEST).</p>
     */
    CANCEL_REQUEST,
    /**
     * Customer has requested a return; awaiting pickup.
     *
     * <p>The customer wants to return a delivered item. From this
     * state, a pickup is scheduled (transition to RETURN_PICKUP).</p>
     */
    RETURN_REQUEST,
    /**
     * Customer has requested a refund; awaiting return pickup.
     *
     * <p>The customer wants a refund for a delivered item. From this
     * state, a pickup is scheduled (transition to RETURN_PICKUP)
     * before the refund can be processed.</p>
     */
    REFUND_REQUEST,
    /**
     * Refund has been processed to the customer.
     *
     * <p>This is a terminal state for refunded orders. The payment
     * has been reversed and the customer has received their money
     * back.</p>
     */
    REFUNDED,
    /**
     * Return/refund/replace pickup has been scheduled.
     *
     * <p>A pickup agent has been assigned to collect the item from
     * the customer. From this state, the next expected transition
     * is to PICKUP_ARRIVED when the agent collects the item.</p>
     */
    RETURN_PICKUP,
    /**
     * Pickup agent has collected the item from the customer.
     *
     * <p>The item has been physically received. From this state,
     * the order can transition to RETURNED, REFUNDED, or
     * ISSUE_REPLACE depending on the resolution.</p>
     */
    PICKUP_ARRIVED,
    /**
     * Replace-specific pickup has been scheduled.
     *
     * <p>A pickup agent has been assigned to collect the original
     * item before sending a replacement. From this state, the next
     * expected transition is to PICKUP_ARRIVED.</p>
     */
    REPLACE_PICKUP,
    /**
     * Customer has requested a replacement; awaiting pickup.
     *
     * <p>The customer wants a replacement item instead of a refund.
     * From this state, a pickup is scheduled (transition to
     * REPLACE_PICKUP) to collect the original item.</p>
     */
    REPLACE_REQUEST,
    /**
     * Replacement item has been dispatched to the customer.
     *
     * <p>The replacement item is in transit. From this state, the
     * next expected transition is to REPLACE_DELIVERED when the
     * customer receives the replacement.</p>
     */
    ISSUE_REPLACE,
    /**
     * Replacement item has been delivered to the customer.
     *
     * <p>The customer has received the replacement item. From this
     * state, the next expected transition is to REPLACED to
     * complete the replacement workflow.</p>
     */
    REPLACE_DELIVERED,
    /**
     * Replacement workflow completed successfully.
     *
     * <p>This is a terminal state for replacement orders. The
     * original item has been collected and the replacement has
     * been delivered.</p>
     */
    REPLACED;

    /**
     * Maps each status to its set of valid next states.
     */
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS;

    static {
        TRANSITIONS = new EnumMap<>(OrderStatus.class);
        TRANSITIONS.put(PLACED, EnumSet.of(CONFIRMED, CANCEL_REQUEST, CANCELLED));
        TRANSITIONS.put(CANCEL_REQUEST, EnumSet.of(CANCELLED));
        TRANSITIONS.put(CONFIRMED, EnumSet.of(SHIPPED));
        TRANSITIONS.put(SHIPPED, EnumSet.of(OUT_FOR_DELIVERY));
        TRANSITIONS.put(OUT_FOR_DELIVERY, EnumSet.of(DELIVERED));
        TRANSITIONS.put(DELIVERED, EnumSet.of(RETURN_REQUEST, REFUND_REQUEST, REPLACE_REQUEST));
        TRANSITIONS.put(RETURN_REQUEST, EnumSet.of(RETURN_PICKUP));
        TRANSITIONS.put(REFUND_REQUEST, EnumSet.of(RETURN_PICKUP));
        TRANSITIONS.put(REPLACE_REQUEST, EnumSet.of(REPLACE_PICKUP));
        TRANSITIONS.put(RETURN_PICKUP, EnumSet.of(PICKUP_ARRIVED));
        TRANSITIONS.put(PICKUP_ARRIVED, EnumSet.of(RETURNED, REFUNDED, ISSUE_REPLACE));
        TRANSITIONS.put(REPLACE_PICKUP, EnumSet.of(PICKUP_ARRIVED));
        TRANSITIONS.put(ISSUE_REPLACE, EnumSet.of(REPLACE_DELIVERED));
        TRANSITIONS.put(REPLACE_DELIVERED, EnumSet.of(REPLACED));
    }

    /**
     * Returns whether a transition from this status to the given target is allowed.
     *
     * @param target the desired target status
     * @return {@code true} if the transition is valid
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (this == target) {
            return true;
        }
        Set<OrderStatus> allowed = TRANSITIONS.get(this);
        return allowed != null && allowed.contains(target);
    }
}
