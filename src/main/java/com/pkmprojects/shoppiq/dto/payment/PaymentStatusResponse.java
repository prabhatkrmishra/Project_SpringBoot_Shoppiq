package com.pkmprojects.shoppiq.dto.payment;

import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.time.Instant;

/**
 * Lightweight response that returns only the current payment status.
 * Used for verify/cancel/refund operations.
 *
 * <p>This is a <b>minimal projection</b> — a stripped-down DTO that returns
 * only the essential status fields. This pattern is useful for action endpoints
 * (verify/refund/cancel) where the full {@link PaymentResponse} would be
 * unnecessarily verbose.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record PaymentStatusResponse(

        Long paymentId,
        String paymentReference,
        PaymentStatus status,
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
