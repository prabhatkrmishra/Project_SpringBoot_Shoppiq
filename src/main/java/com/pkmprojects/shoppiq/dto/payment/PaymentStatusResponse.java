package com.pkmprojects.shoppiq.dto.payment;

import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.time.Instant;

/**
 * Lightweight response returning only the current payment status.
 *
 * <p>This record is a minimal projection containing only the essential
 * status fields needed for payment action endpoints (verify, cancel,
 * refund). It is deliberately stripped down compared to the full
 * {@link PaymentResponse} to minimize payload size on the critical
 * payment processing path where only status confirmation is needed.</p>
 *
 * <p>The static {@link #from(Payment)} factory method extracts only
 * the four status-related fields from the payment entity. This
 * pattern is useful for action endpoints where the full payment
 * detail would be unnecessarily verbose and could expose sensitive
 * gateway information unnecessarily.</p>
 *
 * @param paymentId        unique identifier of the payment record
 * @param paymentReference internal reference code for display and lookup
 * @param status           current payment status (PENDING, PAID, FAILED, REFUNDED)
 * @param refundedAt       timestamp when the payment was refunded;
 *                         null if not refunded
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record PaymentStatusResponse(

        /**
         * Unique identifier of the payment.
         */
        Long paymentId,

        /**
         * Internal payment reference code.
         */
        String paymentReference,

        /**
         * Current payment status.
         */
        PaymentStatus status,

        /**
         * When the payment was refunded. Null if not refunded.
         */
        Instant refundedAt
) {

    /**
     * Maps a {@link Payment} entity to a {@link PaymentStatusResponse}.
     *
     * @param payment source entity
     * @return lightweight status response
     */
    public static PaymentStatusResponse from(Payment payment) {
        return new PaymentStatusResponse(
                payment.getId(),
                payment.getPaymentReference(),
                payment.getPaymentStatus(),
                payment.getRefundedAt()
        );
    }
}
