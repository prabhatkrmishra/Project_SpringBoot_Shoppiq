package com.pkmprojects.shoppiq.enums;

import com.pkmprojects.shoppiq.entity.payment.Payment;

/**
 * <strong>Spring Boot Concept:</strong> Payment lifecycle states for a {@link Payment}.
 *
 * <p>Defines the state machine for payment processing. The lifecycle
 * flows: {@link #PENDING} → {@link #PROCESSING} → {@link #PAID} (success)
 * or {@link #FAILED} (failure). {@link #CANCELLED} represents a payment
 * abandoned before completion. {@link #REFUNDED} is a terminal state
 * for post-settlement reversals.</p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Payment state machine</strong> — Used in conjunction with
 *         the {@link Payment} entity to track the lifecycle of a financial
 *         transaction. Service-layer logic transitions between states based
 *         on gateway callbacks and user actions.</li>
 *     <li><strong>Stored as STRING</strong> — Used with
 *         {@code @Enumerated(EnumType.STRING)} in the JPA entity for
 *         readable database values, making it easy to inspect and query
 *         payment states directly in SQL.</li>
 *     <li><strong>COD lifecycle</strong> — For cash-on-delivery, the payment
 *         stays {@code PENDING} until delivery is confirmed, then transitions
 *         to {@code PAID}.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum PaymentStatus {

    /**
     * Payment record created, awaiting action.
     */
    PENDING,

    /**
     * Payment is currently being processed by the gateway.
     */
    PROCESSING,

    /**
     * Payment has been successfully received.
     */
    PAID,

    /**
     * Payment attempt failed; may be retried.
     */
    FAILED,

    /**
     * Payment has been cancelled before completion.
     */
    CANCELLED,

    /**
     * Payment has been refunded to the customer.
     */
    REFUNDED
}
