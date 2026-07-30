/**
 * Payment gateway abstraction layer using the Strategy pattern.
 *
 * <p>This package defines the pluggable payment provider integration
 * architecture. It uses the Strategy pattern to support multiple payment
 * gateways (Stripe, Razorpay, PayPal, UPI, and COD) without coupling
 * the checkout or payment service to any specific provider. New gateways
 * can be added by implementing the strategy interface and registering
 * a Spring bean.</p>
 *
 * <p>The gateway layer handles payment initiation, verification, and
 * error translation. It abstracts away the differences between gateway
 * APIs and provides a uniform interface for the rest of the application.
 * Gateway-specific configuration is managed through
 * {@link com.pkmprojects.shoppiq.config.PaymentGatewayProperties}.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.gateway;
