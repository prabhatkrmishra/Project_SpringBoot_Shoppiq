package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.enums.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for admin payment management.
 *
 * <p>
 * This DTO provides a comprehensive view of a payment for administrators,
 * including order reference, gateway details, and transaction information.
 * Supports refund operations.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose payment details to admin API.</li>
 *     <li>Support refund operations.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java Records.</li>
 *     <li>Includes order reference for context.</li>
 *     <li>Created using {@link #fromEntity(Payment)}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record AdminPaymentResponse(

        /**
         * Payment identifier.
         */
        Long id,

        /**
         * Internal payment reference.
         */
        String paymentReference,

        /**
         * Order identifier.
         */
        Long orderId,

        /**
         * Order payment reference.
         */
        String orderPaymentReference,

        /**
         * Customer username.
         */
        String customerUsername,

        /**
         * Payment method.
         */
        PaymentMethod paymentMethod,

        /**
         * Payment status.
         */
        PaymentStatus paymentStatus,

        /**
         * Payment gateway used.
         */
        PaymentGateway gateway,

        /**
         * Payment amount.
         */
        BigDecimal amount,

        /**
         * Currency code.
         */
        String currency,

        /**
         * External transaction ID from gateway.
         */
        String transactionId,

        /**
         * Payment creation timestamp.
         */
        Instant createdAt,

        /**
         * Payment completion timestamp.
         */
        Instant paidAt
) {

    /**
     * Creates an {@code AdminPaymentResponse} from a {@link Payment} entity.
     *
     * @param payment payment entity
     * @return mapped response DTO
     */
    public static AdminPaymentResponse fromEntity(Payment payment) {
        Order order = payment.getOrder();
        return new AdminPaymentResponse(
                payment.getId(),
                payment.getPaymentReference(),
                order.getId(),
                payment.getPaymentReference(),
                order.getUser().getUsername(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getGateway(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getTransactionId(),
                payment.getCreatedAt(),
                payment.getPaidAt()
        );
    }
}