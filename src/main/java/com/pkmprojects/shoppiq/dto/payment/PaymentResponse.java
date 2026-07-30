package com.pkmprojects.shoppiq.dto.payment;

import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Full payment detail response for customer and admin payment views.
 *
 * <p>This record exposes comprehensive payment information including
 * internal references, order linkage, gateway details, transaction
 * metadata, and timestamps. It is the response counterpart of the
 * {@code Payment} entity, exposing only non-sensitive fields. It is
 * returned by the payment detail endpoint for both customer order
 * history and admin payment management.</p>
 *
 * <p>The {@code paymentReference} is an internally generated unique
 * code for customer-facing display, while {@code transactionId} is
 * the external identifier returned by the payment gateway after
 * successful processing. The {@code paidAt} and {@code refundedAt}
 * timestamps are populated progressively as the payment lifecycle
 * advances.</p>
 *
 * @param id               unique identifier of the payment record
 * @param orderId          identifier of the order associated with this payment
 * @param paymentReference internal unique reference code for display
 *                         and lookup purposes
 * @param paymentMethod    method used for payment (CREDIT_CARD, UPI, COD)
 * @param status           current payment status (PENDING, PAID, FAILED, REFUNDED)
 * @param gateway          payment gateway used to process the transaction (RAZORPAY)
 * @param amount           monetary amount of the payment in the platform's base currency
 * @param currency         ISO currency code (e.g. INR, USD)
 * @param transactionId    external transaction identifier from the payment
 *                         gateway; null until payment is verified
 * @param paidAt           timestamp when the payment was confirmed as successful;
 *                         null if not yet completed
 * @param refundedAt       timestamp when the payment was refunded;
 *                         null if not refunded
 * @param createdAt        timestamp when the payment record was first created
 * @param updatedAt        timestamp of the most recent modification
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record PaymentResponse(

        /**
         * Unique identifier of the payment.
         */
        Long id,

        /**
         * Associated order identifier.
         */
        Long orderId,

        /**
         * Internal payment reference code.
         */
        String paymentReference,

        /**
         * Payment method used (e.g., CREDIT_CARD, UPI, COD).
         */
        PaymentMethod paymentMethod,

        /**
         * Current payment status.
         */
        PaymentStatus status,

        /**
         * Payment gateway used.
         */
        PaymentGateway gateway,

        /**
         * Payment amount.
         */
        BigDecimal amount,

        /**
         * Currency code (e.g., INR, USD).
         */
        String currency,

        /**
         * External transaction ID from gateway. Null until verified.
         */
        String transactionId,

        /**
         * When the payment was completed. Null until verified.
         */
        Instant paidAt,

        /**
         * When the payment was refunded. Null if not refunded.
         */
        Instant refundedAt,

        /**
         * Entity creation timestamp.
         */
        Instant createdAt,

        /**
         * Entity last update timestamp.
         */
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
