package com.pkmprojects.shoppiq.enums;

/**
 * Supported payment methods for an {@link com.pkmprojects.shoppiq.entity.Order}.
 *
 * <p>
 * The frontend submits one of {@code CREDIT_CARD}, {@code PAYPAL} or
 * {@code STRIPE} (see {@code checkout.html}). These are all online methods and
 * are stored as-is for auditing, while {@link #isOnline()} routes them to the
 * online payment strategy. {@code COD} is the only non-online method.
 * </p>
 *
 * @author PrabhatKrMishra
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
