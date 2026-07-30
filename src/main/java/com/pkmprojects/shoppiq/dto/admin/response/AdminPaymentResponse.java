package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Comprehensive response DTO for admin payment management.
 *
 * <p>This record provides a full view of a payment transaction for
 * administrators, including internal references, order linkage,
 * customer identification, gateway details, and transaction metadata.
 * It is returned by the admin payment list and detail endpoints and
 * is designed for the admin payment management UI where administrators
 * need to review, reconcile, and process refund operations.</p>
 *
 * <p>The {@code paymentReference} is an internally generated unique
 * code used for customer-facing display, while {@code transactionId}
 * is the external identifier returned by the payment gateway after
 * successful processing. The static {@link #fromEntity(Payment)}
 * factory method handles the entity-to-DTO conversion.</p>
 *
 * @param id                    unique identifier of the payment record, auto-generated
 *                              by the database
 * @param paymentReference      internal unique reference code displayed
 *                              to customers and used for payment lookup
 * @param orderId               identifier of the order associated with this payment
 * @param orderPaymentReference order-level payment reference for
 *                              cross-referencing
 * @param customerUsername      username of the customer who made the payment
 * @param paymentMethod         method used for payment (CREDIT_CARD, UPI, COD)
 * @param paymentStatus         current payment status (PENDING, PAID, FAILED, REFUNDED)
 * @param gateway               payment gateway used to process the transaction (RAZORPAY)
 * @param amount                monetary amount of the payment in the platform's base currency
 * @param currency              ISO currency code (e.g. INR, USD)
 * @param transactionId         external transaction identifier from the payment
 *                              gateway; null until payment is verified
 * @param createdAt             timestamp when the payment record was first created
 * @param paidAt                timestamp when the payment was confirmed as successful;
 *                              null if not yet completed
 * @author prabhatkrmishra
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
