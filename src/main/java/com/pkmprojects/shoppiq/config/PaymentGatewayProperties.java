package com.pkmprojects.shoppiq.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.http.client.InetAddressFilter;

import java.net.InetAddress;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 * <strong>Spring Boot Concept:</strong> {@code @ConfigurationProperties}
 * class bound to {@code shoppiq.payment.gateways.*} in
 * {@code application.yaml}.
 *
 * <p>Provides externalized configuration for every payment gateway
 * integration (Razorpay, Stripe, PayPal, UPI). Each gateway is a
 * {@link GatewayConfig} POJO with base URL, API key, API secret, and
 * optional tuning knobs.</p>
 *
 * <p>At startup, validates that every <em>enabled</em> gateway's base
 * URL resolves to a public (non-internal) address, preventing SSRF
 * misconfigurations where a payment gateway could be pointed at an
 * internal service (e.g. {@code http://169.254.169.254/...}).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Getter
@ConfigurationProperties(prefix = "shoppiq.payment.gateways")
public class PaymentGatewayProperties {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayProperties.class);

    private final GatewayConfig razorpay;
    private final GatewayConfig stripe;
    private final GatewayConfig paypal;
    private final GatewayConfig upi;

    public PaymentGatewayProperties(GatewayConfig razorpay, GatewayConfig stripe,
                                    GatewayConfig paypal, GatewayConfig upi) {
        this.razorpay = razorpay != null ? razorpay : new GatewayConfig();
        this.stripe = stripe != null ? stripe : new GatewayConfig();
        this.paypal = paypal != null ? paypal : new GatewayConfig();
        this.upi = upi != null ? upi : new GatewayConfig();
    }

    /**
     * Validates that all enabled gateway base URLs resolve to external
     * (non-internal) addresses at startup.
     *
     * <p>Uses the same {@link InetAddressFilter#externalAddresses()} filter
     * as the {@link RestClientConfig} SSRF protection. If an enabled gateway
     * points to an internal address (loopback, RFC 1918, link-local, cloud
     * metadata, etc.), startup fails with a clear error message.</p>
     *
     * @throws IllegalStateException if an enabled gateway has an internal base URL
     */
    @PostConstruct
    void validateGatewayUrls() {
        InetAddressFilter filter = InetAddressFilter.externalAddresses();

        Map<String, GatewayConfig> gateways = Map.of(
                "razorpay", razorpay,
                "stripe", stripe,
                "paypal", paypal,
                "upi", upi
        );

        for (Map.Entry<String, GatewayConfig> entry : gateways.entrySet()) {
            String name = entry.getKey();
            GatewayConfig config = entry.getValue();

            if (!config.isEnabled()) {
                continue;
            }

            String baseUrl = config.getBaseUrl();
            if (baseUrl == null || baseUrl.isBlank()) {
                log.warn("Payment gateway '{}' is enabled but has no base URL configured", name);
                continue;
            }

            try {
                URI uri = URI.create(baseUrl);
                String host = uri.getHost();
                if (host == null || host.isBlank()) {
                    throw new IllegalStateException(
                            "Payment gateway '%s' base URL '%s' has no valid host".formatted(name, baseUrl));
                }

                InetAddress address = InetAddress.getByName(host);
                if (!filter.matches(address)) {
                    throw new IllegalStateException(
                            "Payment gateway '%s' base URL '%s' resolves to internal address '%s'. "
                                    .formatted(name, baseUrl, address.getHostAddress())
                                    + "SSRF protection requires payment gateways to use external URLs only. "
                                    + "Set the URL to a publicly-routable address or disable the gateway.");
                }

                log.debug("Payment gateway '{}' base URL '{}' resolves to external address '{}'",
                        name, baseUrl, address.getHostAddress());

            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Unable to validate base URL for payment gateway '{}': {}", name, e.getMessage());
            }
        }
    }
}
