package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.config.PaymentGatewayProperties;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.general.payment.PaymentGatewayException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Concrete payment gateway strategy for PayPal.
 *
 * <p>This gateway implements the PayPal checkout flow using the Orders API.
 * On {@link #process(Payment)}, it creates a checkout order with intent
 * CAPTURE and returns the approval URL for the customer. On
 * {@link #verify(Payment, String)}, it captures the approved order to
 * finalize the payment. Authentication uses OAuth2 bearer tokens that are
 * obtained via client credentials and cached for reuse.</p>
 *
 * <p>PayPal handles card processing internally, so the application never
 * sees the customer's payment details. The gateway supports multiple
 * currencies and is the primary gateway for PayPal wallet payments.</p>
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
                         PaymentGatewayProperties properties,
                         Clock clock) {
        super(restClientBuilder, objectMapper,
                properties.getPaypal().getBaseUrl(),
                properties.getPaypal().getApiKey(),
                properties.getPaypal().getApiSecret(),
                clock);
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
                "purchase_units", List.of(Map.of("amount", Map.of(
                        "currency_code", payment.getCurrency(),
                        "value", payment.getAmount().toPlainString())))
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
            payment.setPaidAt(Instant.now(clock));
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
        if (token != null && !token.isExpired(clock)) {
            return token.accessToken;
        }
        synchronized (this) {
            token = cachedToken.get();
            if (token != null && !token.isExpired(clock)) {
                return token.accessToken;
            }
            String response = exchangeForm(HttpMethod.POST, "/v1/oauth2/token",
                    "grant_type=client_credentials", basicAuth(apiKey, apiSecret));
            JsonNode node = parse(response);
            String accessToken = node.get("access_token").asText();
            long expiresIn = node.has("expires_in") ? node.get("expires_in").asLong() : 3600L;
            Token fresh = new Token(accessToken, Instant.now(clock).plusSeconds(expiresIn - 60));
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
        boolean isExpired(Clock clock) {
            return Instant.now(clock).isAfter(expiresAt);
        }
    }
}
