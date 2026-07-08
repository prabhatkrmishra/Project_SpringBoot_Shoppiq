package com.pkmprojects.shoppiq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalised configuration for every payment gateway integration.
 *
 * <p>Bound from {@code shoppiq.payment.gateways.*} in {@code application.yaml}.
 * Only {@code base-url}, {@code api-key} (and where relevant {@code api-secret})
 * are required; the rest are optional tuning knobs.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "shoppiq.payment.gateways")
public class PaymentGatewayProperties {

    private GatewayConfig razorpay = new GatewayConfig();
    private GatewayConfig stripe = new GatewayConfig();
    private GatewayConfig paypal = new GatewayConfig();
    private GatewayConfig upi = new GatewayConfig();

}
