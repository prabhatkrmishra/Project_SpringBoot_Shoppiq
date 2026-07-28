package com.pkmprojects.shoppiq.dto.payment;

import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Full payment detail response.
 *
 * <p>This Java record exposes comprehensive payment information including
 * gateway details, transaction IDs, and timestamps. It is the response
 * counterpart of the {@link com.pkmprojects.shoppiq.entity.payment.Payment}
 * entity, exposing only non-sensitive fields.</p>
 *
 * <p><b>Enum serialization:</b> The {@code paymentMethod}, {@code status},
 * and {@code gateway} fields are Java enums that Spring Boot serializes
 * as strings — no custom serializer needed.</p>
 *
 * @author prabhatkrmishra
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
