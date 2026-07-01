package com.pkmprojects.shoppiq.enums;

/**
 * Payment lifecycle states for a {@link com.pkmprojects.shoppiq.entity.Payment}.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public enum PaymentStatus {

    /**
     * Payment record created, awaiting action.
     */
    PENDING,

    /**
     * Payment is currently being processed by the gateway.
     */
    PROCESSING,

    /**
     * Payment has been successfully received.
     */
    PAID,

    /**
     * Payment attempt failed; may be retried.
     */
    FAILED,

    /**
     * Payment has been cancelled before completion.
     */
    CANCELLED,

    /**
     * Payment has been refunded to the customer.
     */
    REFUNDED
}
