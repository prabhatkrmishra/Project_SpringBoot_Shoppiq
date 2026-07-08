package com.pkmprojects.shoppiq.gateway.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkmprojects.shoppiq.config.PaymentGatewayProperties;
import com.pkmprojects.shoppiq.entity.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.PaymentGatewayException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * PayPal payment gateway integration (global, sandbox).
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
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Component
public class PaypalGateway extends AbstractRestGateway {

    private final AtomicReference<Token> cachedToken = new AtomicReference<>();

    public PaypalGateway(RestClient.Builder restClientBuilder,
                         ObjectMapper objectMapper,
                         PaymentGatewayProperties properties) {
        super(restClientBuilder, objectMapper,
                properties.getPaypal().getBaseUrl(),
                properties.getPaypal().getApiKey(),
                properties.getPaypal().getApiSecret());
    }

    @Override
    public PaymentGateway supports() {
        return PaymentGateway.PAYPAL;
    }

    @Override
    protected String gatewayName() {
        return "PayPal";
    }

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

    @Override
    public void verify(Payment payment, String transactionId) {
        String response = exchange(HttpMethod.POST,
                "/v2/checkout/orders/" + transactionId + "/capture", null, bearer(token()));
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
        String response = exchangeForm(HttpMethod.POST, "/v1/oauth2/token",
                "grant_type=client_credentials", basicAuth(apiKey, apiSecret));
        JsonNode node = parse(response);
        String accessToken = node.get("access_token").asText();
        long expiresIn = node.has("expires_in") ? node.get("expires_in").asLong() : 3600L;
        Token fresh = new Token(accessToken, Instant.now().plusSeconds(expiresIn - 60));
        cachedToken.set(fresh);
        return accessToken;
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
