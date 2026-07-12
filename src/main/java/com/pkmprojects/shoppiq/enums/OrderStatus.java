package com.pkmprojects.shoppiq.enums;

/**
 * Lifecycle states of an {@link com.pkmprojects.shoppiq.entity.Order}.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public enum OrderStatus {

    /**
     * Order has been placed by the customer.
     */
    PLACED,

    /**
     * Order has been confirmed by the store.
     */
    CONFIRMED,

    /**
     * Order has been shipped.
     */
    SHIPPED,

    /**
     * Order is out for delivery.
     */
    OUT_FOR_DELIVERY,

    /**
     * Order has been delivered.
     */
    DELIVERED,

    /**
     * Order has been cancelled.
     */
    CANCELLED,

    /**
     * Order has been returned.
     */
    RETURNED,

    /**
     * Customer has requested cancellation.
     */
    CANCEL_REQUEST,

    /**
     * Customer has requested a return.
     */
    RETURN_REQUEST,

    /**
     * Customer has requested a refund.
     */
    REFUND_REQUEST,

    /**
     * Refund has been processed.
     */
    REFUNDED,

    /**
     * Item pickup scheduled for return or refund.
     */
    RETURN_PICKUP,

    /**
     * Picked-up item has arrived at the warehouse.
     */
    PICKUP_ARRIVED,

    /**
     * Item pickup scheduled for replacement.
     */
    REPLACE_PICKUP,

    /**
     * Customer has requested a replacement.
     */
    REPLACE_REQUEST,

    /**
     * Replacement issue has been logged.
     */
    ISSUE_REPLACE,

    /**
     * Replacement has been delivered to the customer.
     */
    REPLACE_DELIVERED,

    /**
     * Replacement has been completed.
     */
    REPLACED
}
