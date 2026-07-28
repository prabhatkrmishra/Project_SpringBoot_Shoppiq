package com.pkmprojects.shoppiq.enums;

import com.pkmprojects.shoppiq.gateway.payment.PaymentGatewayStrategy;

/**
 * <strong>Spring Boot Concept:</strong> Identifies which payment gateway processed a transaction.
 *
 * <p>
 * {@code NONE} is used for cash-on-delivery orders, where no external
 * gateway is involved. The remaining values represent future integrations
 * that can be plugged in via the {@link PaymentGatewayStrategy}
 * strategy interface without changing the checkout flow.
 * </p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Strategy pattern integration</strong> — This enum works
 *         with the {@code PaymentGatewayStrategy} interface to select the
 *         appropriate gateway implementation at runtime, demonstrating how
 *         enums can drive polymorphic behavior in Spring.</li>
 *     <li><strong>Future-ready design</strong> — Values like {@code STRIPE},
 *         {@code PAYPAL}, and {@code UPI} are defined upfront even if not
 *         yet implemented, making the data model stable.</li>
 *     <li><strong>Stored as STRING in the JPA entity</strong> — Used with
 *         {@code @Enumerated(EnumType.STRING)} in {@link Payment} for
 *         human-readable database values.</li>
 *     <li><strong>NONE for COD</strong> — {@code NONE} represents the
 *         absence of a gateway for cash-on-delivery, keeping the model
 *         consistent (every payment has a gateway value).</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum PaymentGateway {

    /**
     * No gateway — used for COD orders.
     */
    NONE,

    /**
     * Generic online placeholder gateway (used when no real gateway is wired).
     */
    ONLINE,

    /**
     * Razorpay payment gateway.
     */
    RAZORPAY,

    /**
     * Stripe payment gateway.
     */
    STRIPE,

    /**
     * PayPal payment gateway.
     */
    PAYPAL,

    /**
     * UPI payment gateway (via a PSP such as Razorpay/PhonePe/Cashfree).
     */
    UPI
}
