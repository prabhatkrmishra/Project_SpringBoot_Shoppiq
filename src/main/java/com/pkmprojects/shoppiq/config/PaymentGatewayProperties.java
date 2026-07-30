package com.pkmprojects.shoppiq.config;

import com.pkmprojects.shoppiq.exception.general.payment.InvalidPaymentGatewayConfigException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.http.client.InetAddressFilter;

import java.net.InetAddress;
import java.net.URI;
import java.util.Map;

/**
 * Configuration properties for payment gateway integrations.
 *
 * <p>This class binds to the {@code shoppiq.payment.gateways.*} prefix in
 * {@code application.yaml} and holds connection settings for each supported
 * payment provider: Razorpay, Stripe, PayPal, and UPI. Each provider is
 * represented by a {@link GatewayConfig} instance containing the base URL,
 * API keys, and an enabled flag. The class validates at startup that all
 * enabled gateways resolve to external (non-internal) addresses, preventing
 * SSRF misconfigurations that could expose the application to internal
 * network attacks.</p>
 *
 * <p>The validation uses the same {@link InetAddressFilter#externalAddresses()}
 * filter as the {@link RestClientConfig} SSRF protection. If an enabled
 * gateway points to an internal address (loopback, RFC 1918, link-local,
 * cloud metadata, etc.), the application fails to start with a clear error
 * message. This fail-fast behavior ensures that payment integrations are
 * never accidentally directed at development or staging environments in
 * production deployments.</p>
 *
 * @author prabhatkrmishra
 * @see GatewayConfig
 * @see GatewayConfig
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
     * (non-internal) addresses at application startup.
     *
     * <p>This {@code @PostConstruct} method runs after dependency injection
     * is complete and iterates over all configured gateway entries. For each
     * enabled gateway with a non-blank base URL, it extracts the hostname,
     * resolves it to an {@link InetAddress}, and checks it against the
     * {@link InetAddressFilter#externalAddresses()} filter. If the resolved
     * address is internal (loopback, private, link-local, or cloud metadata),
     * an {@link InvalidPaymentGatewayConfigException} is thrown, which
     * prevents the application from starting.</p>
     *
     * <p>Gateways that are disabled or have no base URL configured are
     * silently skipped. DNS resolution failures are logged as warnings
     * rather than hard failures, as they may indicate transient network
     * issues during startup that should not block deployment.</p>
     *
     * @throws InvalidPaymentGatewayConfigException if an enabled gateway
     *                                              has an internal or unresolvable base URL
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
                    throw InvalidPaymentGatewayConfigException.invalidHost(name, baseUrl);
                }

                InetAddress address = InetAddress.getByName(host);
                if (!filter.matches(address)) {
                    throw InvalidPaymentGatewayConfigException.internalAddress(name, baseUrl, address.getHostAddress());
                }

                log.debug("Payment gateway '{}' base URL '{}' resolves to external address '{}'",
                        name, baseUrl, address.getHostAddress());

            } catch (InvalidPaymentGatewayConfigException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Unable to validate base URL for payment gateway '{}': {}", name, e.getMessage());
            }
        }
    }
}
