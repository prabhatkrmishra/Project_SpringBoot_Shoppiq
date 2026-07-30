/**
 * Concrete payment gateway implementations for each supported provider.
 *
 * <p>This package contains the strategy interface, registry, abstract base
 * class, and concrete implementations for Stripe, Razorpay, PayPal, UPI,
 * and COD payment providers. The {@link PaymentGatewayRegistry} collects
 * all strategy beans and resolves the correct implementation based on the
 * customer's selected payment method.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.gateway.payment;
