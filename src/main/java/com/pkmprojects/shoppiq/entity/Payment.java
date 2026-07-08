package com.pkmprojects.shoppiq.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
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
 * <p>
 * The payment entity tracks the full payment lifecycle — from creation
 * through processing, success, failure, cancellation, and refund.
 * Each order has exactly one payment record.
 * </p>
 *
 * <h2>Key Fields</h2>
 * <ul>
 *   <li>{@code paymentReference} — internal stable reference (e.g. PAY-20260701-001).</li>
 *   <li>{@code transactionId} — external ID returned by the payment gateway.</li>
 *   <li>{@code gatewayResponse} — raw response from the gateway (for audit).</li>
 *   <li>{@code gateway} — which gateway processed the transaction.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li>COD payments use {@code gateway = NONE} and remain {@code PENDING}
 *       until delivery is confirmed.</li>
 *   <li>Online payments start as {@code PENDING}, move to {@code PROCESSING}
 *       during gateway interaction, and resolve to {@code PAID} or {@code FAILED}.</li>
 *   <li>Extends {@link AuditableEntity} for id, version, and timestamps.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
     * <p>One order → one payment.</p>
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
     * Internal, human-readable payment reference.
     *
     * <p>Format: {@code PAY-yyyyMMdd-<orderId>} (e.g. PAY-20260701-42).</p>
     */
    @Column(name = "payment_reference", nullable = false, unique = true, length = 50)
    private String paymentReference;

    /**
     * Payment method: COD or ONLINE.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    /**
     * Current lifecycle status of this payment.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    /**
     * Which external gateway was used (NONE for COD).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentGateway gateway;

    /**
     * Amount to be paid (= order grandTotal).
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * Currency code (e.g. INR, USD).
     */
    @Column(nullable = false, length = 10)
    private String currency;

    /**
     * External transaction ID returned by the payment gateway.
     *
     * <p>Null for COD or until the gateway responds.</p>
     */
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    /**
     * Gateway-assigned order/payment-intent/collect identifier.
     *
     * <p>Null until the payment is initiated with the gateway. Used to
     * reconcile/verify the payment and to make re-initiation idempotent.</p>
     */
    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    /**
     * Raw JSON response from the payment gateway, stored for audit purposes.
     *
     * <p>Null for COD orders.</p>
     */
    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    /**
     * Timestamp at which the payment transitioned to {@code PAID}.
     *
     * <p>Null until payment is confirmed.</p>
     */
    @Column(name = "paid_at")
    private Instant paidAt;

    /**
     * Timestamp at which the payment was refunded.
     *
     * <p>Null until the payment is refunded.</p>
     */
    @Column(name = "refunded_at")
    private Instant refundedAt;
}
