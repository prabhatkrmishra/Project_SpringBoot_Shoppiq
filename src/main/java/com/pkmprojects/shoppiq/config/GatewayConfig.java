package com.pkmprojects.shoppiq.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

/**
 * <strong>Spring Boot Concept:</strong> Nested configuration POJO for
 * per-gateway connection settings (Razorpay, Stripe, PayPal, UPI).
 *
 * <p>Bound from {@code shoppiq.payment.gateways.<gateway>.*} via
 * {@link PaymentGatewayProperties}. Uses {@code @JsonIgnore} on
 * sensitive fields to prevent secret exposure in API responses.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Setter
@Getter
public class GatewayConfig {

    /**
     * Gateway REST base URL (no trailing slash).
     */
    private String baseUrl;

    /**
     * Public API key / client id.
     */
    private String apiKey;

    /**
     * API secret / client secret (never exposed in responses).
     */
    @JsonIgnore
    private String apiSecret;

    /**
     * Whether this gateway is active.
     */
    private boolean enabled = false;

    /**
     * UPI-specific: merchant VPA that collects payments.
     */
    private String merchantVpa;
}
