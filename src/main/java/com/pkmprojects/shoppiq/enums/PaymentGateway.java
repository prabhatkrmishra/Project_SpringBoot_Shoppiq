package com.pkmprojects.shoppiq.enums;

import com.pkmprojects.shoppiq.gateway.payment.PaymentGatewayStrategy;

/**
 * Identifies which payment gateway processed a transaction.
 *
 * <p>
 * {@code NONE} is used for cash-on-delivery orders, where no external
 * gateway is involved. The remaining values represent future integrations
 * that can be plugged in via the {@link PaymentGatewayStrategy}
 * strategy interface without changing the checkout flow.
 * </p>
 *
 * @author PrabhatKrMishra
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
