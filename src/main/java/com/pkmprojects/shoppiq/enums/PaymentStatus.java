package com.pkmprojects.shoppiq.enums;

import com.pkmprojects.shoppiq.entity.payment.Payment;

/**
 * Payment lifecycle states for a {@link Payment}.
 *
 * <p>This enum models the complete payment lifecycle from creation through
 * settlement or failure. Payments flow from {@link #PENDING} through
 * {@link #PROCESSING} to {@link #PAID} (success) or {@link #FAILED}
 * (failure). {@link #CANCELLED} represents abandonment before completion,
 * and {@link #REFUNDED} is a terminal state for post-settlement reversals.</p>
 *
 * <p>The payment status is independent of the order status but is
 * closely related. For example, an order can only be confirmed after
 * its payment reaches PAID status. Refunds require the payment to be
 * in PAID status before processing.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum PaymentStatus {

    /**
     * Payment record created, awaiting customer action.
     *
     * <p>The payment has been initiated but the customer has not yet
     * completed the payment flow. For online payments, this means the
     * customer has not yet been redirected to the gateway. For COD,
     * this means the payment is awaiting delivery.</p>
     */
    PENDING,

    /**
     * Payment is currently being processed by the gateway.
     *
     * <p>The customer has submitted payment and the gateway is
     * processing the transaction. This is a transient state that
     * should resolve quickly to PAID or FAILED.</p>
     */
    PROCESSING,

    /**
     * Payment has been successfully received and confirmed.
     *
     * <p>The gateway has confirmed that the payment was completed
     * successfully. This is the required state before an order can
     * be confirmed and shipped.</p>
     */
    PAID,

    /**
     * Payment attempt failed; may be retried.
     *
     * <p>The gateway rejected the payment or a timeout occurred.
     * The customer may retry the payment or choose a different
     * payment method. The order remains in its current state until
     * a successful payment is received.</p>
     */
    FAILED,

    /**
     * Payment has been cancelled before completion.
     *
     * <p>The customer or system cancelled the payment before it was
     * processed. This is a terminal state for the payment. The order
     * may be cancelled or the customer may initiate a new payment.</p>
     */
    CANCELLED,

    /**
     * Payment has been refunded to the customer.
     *
     * <p>The full or partial payment amount has been returned to the
     * customer. This is a terminal state for the payment. Refunds
     * are triggered by order returns or customer service decisions.</p>
     */
    REFUNDED
}
