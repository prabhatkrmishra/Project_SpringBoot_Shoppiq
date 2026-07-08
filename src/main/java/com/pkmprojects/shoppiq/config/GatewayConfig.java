package com.pkmprojects.shoppiq.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Per-gateway connection settings.
 *
 * <p>Used by {@link PaymentGatewayProperties} to bind individual gateway
 * configuration from {@code shoppiq.payment.gateways.*}.</p>
 *
 * @author PrabhatKrMishra
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
