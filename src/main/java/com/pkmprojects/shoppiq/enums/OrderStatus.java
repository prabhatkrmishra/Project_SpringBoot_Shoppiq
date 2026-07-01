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
    RETURNED
}
