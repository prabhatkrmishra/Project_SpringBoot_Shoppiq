package com.pkmprojects.shoppiq.enums;

import com.pkmprojects.shoppiq.entity.order.Order;

/**
 * <strong>Spring Boot Concept:</strong> Supported payment methods for an {@link Order}.
 *
 * <p>
 * The frontend submits one of {@code CREDIT_CARD}, {@code PAYPAL} or
 * {@code STRIPE} (see {@code checkout.html}). These are all online methods and
 * are stored as-is for auditing, while {@link #isOnline()} routes them to the
 * online payment strategy. {@code COD} is the only non-online method.
 * </p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Enum with behavior</strong> — The {@code isOnline()} method
 *         adds logic to the enum itself, demonstrating that enums in Java can
 *         have methods and are not just constant containers.</li>
 *     <li><strong>Frontend-backend alignment</strong> — The enum values map
 *         directly to what the frontend sends in the checkout form, ensuring
 *         type safety between the UI and the service layer.</li>
 *     <li><strong>Stored as STRING</strong> — Used with
 *         {@code @Enumerated(EnumType.STRING)} in the {@link Order} entity
 *         for human-readable database values.</li>
 *     <li><strong>COD as offline flag</strong> — The distinction between
 *         online (gateway-required) and offline (COD) payments drives the
 *         checkout flow logic: online methods initiate a gateway transaction,
 *         while COD skips it.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum PaymentMethod {

    /**
     * Cash on delivery — no external gateway.
     */
    COD,

    /**
     * Generic online payment (fallback for any online method).
     */
    ONLINE,

    /**
     * Credit / debit card payment (online).
     */
    CREDIT_CARD,

    /**
     * PayPal payment (online).
     */
    PAYPAL,

    /**
     * Stripe payment (online).
     */
    STRIPE,

    /**
     * UPI payment (online, India) — Google Pay, PhonePe, Paytm, BHIM.
     */
    UPI;

    /**
     * Whether this method requires an external (online) gateway.
     *
     * <p>Only {@link #COD} is offline; every other method is online.</p>
     *
     * @return {@code true} if an online gateway should process this method
     */
    public boolean isOnline() {
        return this != COD;
    }
}
