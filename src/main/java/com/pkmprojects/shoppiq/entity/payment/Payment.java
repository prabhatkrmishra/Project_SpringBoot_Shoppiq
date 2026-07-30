package com.pkmprojects.shoppiq.entity.payment;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents the payment record associated with a single {@link Order}.
 *
 * <p>Tracks the full payment lifecycle from creation through processing,
 * success, failure, cancellation, and refund. Each order has exactly one
 * payment record, enforced by a unique constraint on the {@code order_id}
 * column. COD payments use {@code gateway = NONE} and remain
 * {@code PENDING} until delivery is confirmed by the fulfillment team.</p>
 *
 * <p>Stores an internal {@code paymentReference} for customer support
 * and an external {@code transactionId} returned by the payment gateway.
 * The raw gateway response is preserved as a TEXT column for audit
 * purposes and troubleshooting. The {@code gatewayPaymentId} field
 * supports idempotent re-initiation with the payment provider.</p>
 *
 * @author prabhatkrmishra
 * @see Order
 * @since 1.0.0
 */
@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payments_order",
                        columnNames = "order_id"
                ),
                @UniqueConstraint(
                        name = "uk_payments_reference",
                        columnNames = "payment_reference"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Payment extends AuditableEntity {

    /**
     * Order this payment belongs to.
     *
     * <p>One-to-one relationship: one order produces exactly one payment
     * record. The {@code order_id} column carries a unique constraint
     * to enforce this invariant at the database level. The order
     * reference is lazily loaded to avoid unnecessary joins when
     * querying payment status independently.</p>
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_payments_order")
    )
    private Order order;

    /**
     * Internal, human-readable payment reference used for customer
     * support lookups and audit trails.
     *
     * <p>Format: {@code PAY-yyyyMMdd-<orderId>} (e.g. PAY-20260701-42).
     * Must be globally unique across all payment records. This reference
     * is displayed to customers in confirmation emails and order history
     * for easy identification during support inquiries.</p>
     */
    @Column(name = "payment_reference", nullable = false, unique = true, length = 50)
    private String paymentReference;

    /**
     * Payment method selected by the customer: {@code COD} for
     * cash-on-delivery or {@code ONLINE} for digital payment.
     *
     * <p>Stored as a string enum with a maximum length of 20 characters.
     * This field determines the payment processing flow: COD payments
     * remain pending until delivery confirmation, while ONLINE payments
     * are routed through the configured payment gateway.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    /**
     * Current lifecycle status of this payment, tracking its progression
     * from initiation through completion or failure.
     *
     * <p>Stored as a string enum with a maximum length of 20 characters.
     * Status transitions include PENDING, PROCESSING, PAID, FAILED,
     * CANCELLED, and REFUNDED. Each transition triggers corresponding
     * order status updates and customer notification emails.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    /**
     * External payment gateway used to process this transaction.
     *
     * <p>Set to {@code NONE} for cash-on-delivery (COD) orders where
     * no digital payment gateway is involved. For ONLINE payments,
     * this field identifies the specific gateway (e.g. Razorpay,
     * Stripe) used for processing. Stored as a string enum with a
     * maximum length of 20 characters.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentGateway gateway;

    /**
     * Total amount to be collected from the customer, equal to the
     * order's grand total.
     *
     * <p>Stored denormalized at payment creation time to ensure
     * historical accuracy. This is the authoritative amount used for
     * gateway transactions, refund calculations, and financial
     * reconciliation. Precision is 12 digits total with 2 decimal
     * places.</p>
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * ISO 4217 currency code for the payment amount (e.g. "INR", "USD").
     *
     * <p>Required field with a maximum length of 10 characters. Determines
     * the currency context for gateway processing and display in customer
     * -facing payment confirmations. Must match the store's configured
     * base currency for domestic transactions.</p>
     */
    @Column(nullable = false, length = 10)
    private String currency;

    /**
     * External transaction ID returned by the payment gateway upon
     * successful processing.
     *
     * <p>Null for COD orders or until the gateway responds with a
     * confirmation. This identifier is used for gateway-side lookups,
     * refund processing, and dispute resolution. Maximum length of
     * 100 characters to accommodate various gateway formats.</p>
     */
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    /**
     * Gateway-assigned order or payment-intent identifier used for
     * idempotent re-initiation and reconciliation.
     *
     * <p>Null until the payment is initiated with the gateway. This
     * identifier allows the system to re-query the gateway for payment
     * status without creating duplicate charges. Used primarily in
     * retry flows and webhook reconciliation. Maximum length of 100
     * characters.</p>
     */
    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    /**
     * Raw JSON response from the payment gateway, stored verbatim for
     * audit purposes and troubleshooting.
     *
     * <p>Preserved exactly as received from the gateway without parsing
     * or transformation. Null for COD orders where no gateway interaction
     * occurs. This TEXT column provides a complete audit trail for
     * payment disputes, gateway issues, and compliance reviews.</p>
     */
    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    /**
     * Timestamp at which the payment transitioned to the {@code PAID}
     * status, confirming successful collection.
     *
     * <p>Null until payment is confirmed by the gateway or by manual
     * confirmation for COD orders. This timestamp marks the point at
     * which the order becomes eligible for fulfillment processing.
     * Stored as an {@link Instant} for timezone independence.</p>
     */
    @Column(name = "paid_at")
    private Instant paidAt;

    /**
     * Timestamp at which the payment was refunded to the customer.
     *
     * <p>Null until a refund is processed. Populated when the refund
     * is confirmed by the gateway or manually recorded for COD
     * refunds. This timestamp is used for refund reporting, financial
     * reconciliation, and customer support inquiries.</p>
     */
    @Column(name = "refunded_at")
    private Instant refundedAt;
}
