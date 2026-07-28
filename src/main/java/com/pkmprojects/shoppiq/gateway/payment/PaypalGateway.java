package com.pkmprojects.shoppiq.gateway.payment;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.pkmprojects.shoppiq.config.PaymentGatewayProperties;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.general.payment.PaymentGatewayException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <strong>Spring Boot Concept:</strong> Concrete payment gateway strategy
 * for PayPal, extending {@link AbstractRestGateway} and implementing the
 * {@link PaymentGatewayStrategy} contract.
 *
 * <p>Flow: {@link #process(Payment)} creates a checkout order
 * ({@code POST /v2/checkout/orders}, intent {@code CAPTURE});
 * {@link #verify(Payment, String)} captures the order
 * ({@code POST /v2/checkout/orders/{id}/capture}) and marks the payment
 * {@code PAID} when the capture status is {@code COMPLETED}.</p>
 *
 * <p>PayPal's REST API requires a bearer token obtained from the OAuth2
 * token endpoint; the token is cached until shortly before expiry.</p>
 *
 * <p><strong>Educational value:</strong> This class demonstrates:
 * <ul>
 *   <li><strong>Template Method</strong> — reuses the HTTP infrastructure
 *       from {@code AbstractRestGateway} (exchange, error handling, JSON
 *       parsing) and implements only PayPal-specific logic.</li>
 *   <li><strong>Token caching with double-checked locking</strong> — the
 *       OAuth2 bearer token is cached in an {@link java.util.concurrent.atomic.AtomicReference}
 *       with a synchronized fallback for thread-safe lazy initialisation.</li>
 *   <li><strong>Spring constructor injection</strong> — receives
 *       {@code RestClient.Builder}, {@code JsonMapper}, and typed
 *       configuration via {@code PaymentGatewayProperties}.</li>
 *   <li><strong>Idempotent processing</strong> — if a gateway order ID
 *       already exists, the process method skips the API call.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Component
public final class PaypalGateway extends AbstractRestGateway {

    private final AtomicReference<Token> cachedToken = new AtomicReference<>();

    /**
     * Constructs a PayPal gateway with the given HTTP client, JSON mapper,
     * and gateway-specific configuration.
     *
     * @param restClientBuilder builder for {@link RestClient}
     * @param objectMapper      Jackson 3 {@link JsonMapper}
     * @param properties        typed payment gateway properties
     */
    public PaypalGateway(RestClient.Builder restClientBuilder,
                         JsonMapper objectMapper,
                         PaymentGatewayProperties properties) {
        super(restClientBuilder, objectMapper,
                properties.getPaypal().getBaseUrl(),
                properties.getPaypal().getApiKey(),
                properties.getPaypal().getApiSecret());
    }

    /**
     * Returns the gateway type handled by this strategy.
     *
     * @return {@link PaymentGateway#PAYPAL}
     */
    @Override
    public PaymentGateway supports() {
        return PaymentGateway.PAYPAL;
    }

    /**
     * Returns the human-readable gateway name for error messages.
     *
     * @return {@code "PayPal"}
     */
    @Override
    protected String gatewayName() {
        return "PayPal";
    }

    /**
     * Creates a PayPal checkout order with intent {@code CAPTURE}.
     *
     * <p>If a gateway order ID already exists the payment is simply
     * marked {@code PROCESSING} (idempotent re-entry).</p>
     *
     * @param payment the payment to process
     */
    @Override
    public void process(Payment payment) {
        if (payment.getGatewayPaymentId() != null) {
            payment.setPaymentStatus(PaymentStatus.PROCESSING);
            return;
        }

        Map<String, Object> body = Map.of(
                "intent", "CAPTURE",
                "purchase_units", Map.of("amount", Map.of(
                        "currency_code", payment.getCurrency(),
                        "value", payment.getAmount().toString()))
        );

        String response = exchange(HttpMethod.POST, "/v2/checkout/orders", body, bearer(token()));
        String orderId = parse(response).get("id").asText();

        payment.setGatewayPaymentId(orderId);
        payment.setGateway(PaymentGateway.PAYPAL);
        payment.setPaymentStatus(PaymentStatus.PROCESSING);
        payment.setGatewayResponse(response);
    }

    /**
     * Captures a PayPal order and verifies the capture status.
     *
     * @param payment       the payment to verify
     * @param transactionId the PayPal order ID
     * @throws PaymentGatewayException if the capture status is not
     *                                 {@code COMPLETED}
     */
    @Override
    public void verify(Payment payment, String transactionId) {
        String sanitized = sanitizeTransactionId(transactionId);
        String response = exchange(HttpMethod.POST,
                "/v2/checkout/orders/" + sanitized + "/capture", null, bearer(token()));
        JsonNode root = parse(response);
        String status = root.path("status").asText();

        if ("COMPLETED".equals(status)) {
            payment.setTransactionId(transactionId);
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(Instant.now());
            payment.setGatewayResponse(response);
        } else {
            throw new PaymentGatewayException(
                    "PayPal order '%s' capture status is not COMPLETED (status=%s).".formatted(transactionId, status));
        }
    }

    /**
     * Returns a valid OAuth2 bearer token, fetching and caching one if absent
     * or expired.
     *
     * @return bearer token
     */
    private String token() {
        Token token = cachedToken.get();
        if (token != null && !token.isExpired()) {
            return token.accessToken;
        }
        synchronized (this) {
            token = cachedToken.get();
            if (token != null && !token.isExpired()) {
                return token.accessToken;
            }
            String response = exchangeForm(HttpMethod.POST, "/v1/oauth2/token",
                    "grant_type=client_credentials", basicAuth(apiKey, apiSecret));
            JsonNode node = parse(response);
            String accessToken = node.get("access_token").asText();
            long expiresIn = node.has("expires_in") ? node.get("expires_in").asLong() : 3600L;
            Token fresh = new Token(accessToken, Instant.now().plusSeconds(expiresIn - 60));
            cachedToken.set(fresh);
            return accessToken;
        }
    }

    /**
     * Cached OAuth2 token with an expiry instant.
     *
     * @param accessToken token value
     * @param expiresAt   instant after which the token must be refreshed
     */
    private record Token(String accessToken, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
