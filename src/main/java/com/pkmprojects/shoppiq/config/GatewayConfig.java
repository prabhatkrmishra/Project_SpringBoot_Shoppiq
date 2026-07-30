package com.pkmprojects.shoppiq.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

/**
 * Nested configuration POJO for per-gateway connection settings.
 *
 * <p>This class holds the connection parameters for a single payment
 * gateway provider. It is bound from the
 * {@code shoppiq.payment.gateways.<gateway>.*} properties via
 * {@link PaymentGatewayProperties}. Each gateway (Razorpay, Stripe,
 * PayPal, UPI) gets its own instance of this class with provider-specific
 * values.</p>
 *
 * <p>The {@link #apiSecret} field is annotated with {@code @JsonIgnore} to
 * prevent the secret from appearing in JSON serialization output, such as
 * actuator endpoints or logging. The {@link #enabled} flag controls whether
 * the gateway is active and available for payment processing. Disabled
 * gateways are excluded from the payment method selection UI and from
 * the gateway registry.</p>
 *
 * @author prabhatkrmishra
 * @see PaymentGatewayProperties
 * @since 1.0.0
 */
@Setter
@Getter
public class GatewayConfig {

    /**
     * Gateway REST base URL (no trailing slash).
     *
     * <p>The full URL used to communicate with the payment provider's API.
     * For example, {@code "https://api.razorpay.com"} for Razorpay or
     * {@code "https://api.stripe.com"} for Stripe. This URL is validated
     * at startup by {@link PaymentGatewayProperties} to ensure it resolves
     * to an external address.</p>
     */
    private String baseUrl;

    /**
     * Public API key or client ID used for authentication with the provider.
     *
     * <p>This value is safe to include in client-side code (e.g., Stripe's
     * publishable key) and is used by the gateway implementation to
     * identify the merchant account when making API calls.</p>
     */
    private String apiKey;

    /**
     * API secret or client secret used for server-to-server authentication.
     *
     * <p>This value must never be exposed in responses, logs, or client-side
     * code. It is annotated with {@code @JsonIgnore} to prevent accidental
     * serialization. The secret is used to sign requests and verify
     * webhooks from the payment provider.</p>
     */
    @JsonIgnore
    private String apiSecret;

    /**
     * Whether this gateway is active and available for payment processing.
     *
     * <p>When set to {@code false}, the gateway is excluded from the
     * payment method selection UI and from the gateway registry. This
     * allows administrators to disable a provider without removing its
     * configuration.</p>
     */
    private boolean enabled = false;

    /**
     * UPI-specific: merchant Virtual Payment Address that collects payments.
     *
     * <p>This field is only relevant for UPI gateway configuration. It
     * specifies the VPA (e.g., {@code "merchant@upi"}) that receives
     * payments when a customer chooses the UPI payment method. For other
     * gateway types, this field is ignored.</p>
     */
    private String merchantVpa;
}
