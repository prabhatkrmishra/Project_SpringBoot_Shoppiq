package com.pkmprojects.shoppiq.dto.payment;

import com.pkmprojects.shoppiq.entity.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Full payment detail response.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record PaymentResponse(

        Long id,
        Long orderId,
        String paymentReference,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        PaymentGateway gateway,
        BigDecimal amount,
        String currency,
        String transactionId,
        Instant paidAt,
        Instant refundedAt,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * Maps a {@link Payment} entity to a {@link PaymentResponse}.
     *
     * @param payment source entity
     * @return response DTO
     */
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getPaymentReference(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getGateway(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getTransactionId(),
                payment.getPaidAt(),
                payment.getRefundedAt(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
