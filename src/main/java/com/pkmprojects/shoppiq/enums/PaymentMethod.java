package com.pkmprojects.shoppiq.enums;

import com.pkmprojects.shoppiq.entity.order.Order;

/**
 * Supported payment methods for an {@link Order}.
 *
 * <p>This enum defines all payment methods available to customers at
 * checkout. Online methods ({@link #CREDIT_CARD}, {@link #PAYPAL},
 * {@link #STRIPE}, {@link #UPI}) route to an external gateway via
 * the {@link PaymentGatewayStrategy} strategy pattern. {@link #COD}
 * is the only offline method and does not require gateway integration.</p>
 *
 * <p>The {@link #isOnline()} method determines whether a gateway is
 * required for processing. This method is used by the checkout service
 * to decide whether to redirect the customer to a payment gateway or
 * to process the order as a cash-on-delivery transaction.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum PaymentMethod {

    /**
     * Cash on delivery — no external gateway required.
     *
     * <p>The customer pays in cash when the order is delivered. This is
     * the only offline payment method and does not require a payment
     * gateway integration. A COD surcharge may apply.</p>
     */
    COD,

    /**
     * Generic online payment fallback for any online method.
     *
     * <p>This is a catch-all for online payments that do not have a
     * specific enum value. It routes to the default online gateway
     * configuration.</p>
     */
    ONLINE,

    /**
     * Credit or debit card payment (online).
     *
     * <p>Card payments are processed through the Stripe or Razorpay
     * gateway, depending on the configured payment provider. The
     * customer enters their card details on the gateway's hosted
     * payment page.</p>
     */
    CREDIT_CARD,

    /**
     * PayPal payment (online).
     *
     * <p>The customer is redirected to PayPal to complete the payment.
     * PayPal handles card processing internally, so the application
     * never sees the customer's card details.</p>
     */
    PAYPAL,

    /**
     * Stripe payment (online).
     *
     * <p>Card payments processed through the Stripe gateway. Stripe
     * provides a hosted payment page or embedded form for collecting
     * card details securely.</p>
     */
    STRIPE,

    /**
     * UPI payment (online, India) — Google Pay, PhonePe, Paytm, BHIM.
     *
     * <p>Unified Payments Interface payments are popular in India.
     * The customer scans a QR code or enters their UPI ID to
     * authorize the payment through their UPI app.</p>
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
