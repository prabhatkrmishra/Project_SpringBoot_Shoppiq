package com.pkmprojects.shoppiq.gateway.payment;

import tools.jackson.databind.json.JsonMapper;
import com.pkmprojects.shoppiq.config.PaymentGatewayProperties;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.general.payment.PaymentGatewayException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

/**
 * <strong>Spring Boot Concept:</strong> Concrete payment gateway strategy
 * for Stripe (global), extending {@link AbstractRestGateway}.
 *
 * <p>Flow: {@link #process(Payment)} creates a PaymentIntent
 * ({@code POST /v1/payment_intents}); {@link #verify(Payment, String)} fetches
 * the intent by id ({@code GET /v1/payment_intents/{id}}) and marks the payment
 * {@code PAID} once its status is {@code succeeded}.</p>
 *
 * <p><strong>Educational value:</strong> Stripe differs from Razorpay and
 * PayPal in several interesting ways:
 * <ul>
 *   <li><strong>Bearer auth</strong> — Stripe uses the API key directly as
 *       a bearer token (no separate OAuth token endpoint).</li>
 *   <li><strong>Idempotent process</strong> — on re-processing, Stripe checks
 *       if a gateway ID already exists and skips the API call, just like the
 *       other gateways.</li>
 *   <li><strong>Immediate status check</strong> — the initial
 *       {@code createPaymentIntent} response already contains the status;
 *       if it's already {@code succeeded}, the payment is marked PAID
 *       immediately instead of going to PROCESSING.</li>
 *   <li><strong>Lowercase currency</strong> — Stripe expects the currency
 *       code in lowercase (e.g. {@code usd}), unlike Razorpay which expects
 *       uppercase (e.g. {@code INR}).</li>
 * </ul>
 * These differences illustrate how the Strategy pattern accommodates
 * provider-specific nuances behind a uniform interface.
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Component
public final class StripeGateway extends AbstractRestGateway {

    /**
     * Constructs a Stripe gateway with the given HTTP client, JSON mapper,
     * and gateway-specific configuration.
     *
     * @param restClientBuilder builder for {@link RestClient}
     * @param objectMapper      Jackson 3 {@link JsonMapper}
     * @param properties        typed payment gateway properties
     */
    public StripeGateway(RestClient.Builder restClientBuilder,
                         JsonMapper objectMapper,
                         PaymentGatewayProperties properties,
                         Clock clock) {
        super(restClientBuilder, objectMapper,
                properties.getStripe().getBaseUrl(),
                properties.getStripe().getApiKey(),
                properties.getStripe().getApiSecret(),
                clock);
    }

    /**
     * Returns the gateway type handled by this strategy.
     *
     * @return {@link PaymentGateway#STRIPE}
     */
    @Override
    public PaymentGateway supports() {
        return PaymentGateway.STRIPE;
    }

    /**
     * Returns the human-readable gateway name for error messages.
     *
     * @return {@code "Stripe"}
     */
    @Override
    protected String gatewayName() {
        return "Stripe";
    }

    /**
     * Creates a Stripe PaymentIntent for the given payment.
     *
     * <p>If a gateway payment ID already exists the payment is simply
     * marked {@code PROCESSING} (idempotent re-entry). If the PaymentIntent
     * is already {@code succeeded} the payment is marked {@code PAID}
     * immediately.</p>
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
                "amount", toMinorUnits(payment.getAmount()),
                "currency", payment.getCurrency().toLowerCase(),
                "automatic_payment_methods", Map.of("enabled", true)
        );

        String response = exchange(HttpMethod.POST, "/payment_intents", body, bearer(apiKey));
        tools.jackson.databind.JsonNode node = parse(response);
        String intentId = node.get("id").asText();
        String status = node.get("status").asText();

        payment.setGatewayPaymentId(intentId);
        payment.setGateway(PaymentGateway.STRIPE);
        payment.setPaymentStatus("succeeded".equals(status) ? PaymentStatus.PAID : PaymentStatus.PROCESSING);
        payment.setGatewayResponse(response);
    }

    /**
     * Verifies a Stripe PaymentIntent by fetching its status from the
     * Stripe API.
     *
     * @param payment       the payment to verify
     * @param transactionId the Stripe PaymentIntent ID
     * @throws PaymentGatewayException if the PaymentIntent status is not
     *                                 {@code succeeded}
     */
    @Override
    public void verify(Payment payment, String transactionId) {
        String sanitized = sanitizeTransactionId(transactionId);
        String response = exchange(HttpMethod.GET, "/payment_intents/" + sanitized, null, bearer(apiKey));
        String status = parse(response).get("status").asText();

        if ("succeeded".equals(status)) {
            payment.setTransactionId(transactionId);
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(Instant.now(clock));
            payment.setGatewayResponse(response);
        } else {
            throw new PaymentGatewayException(
                    "Stripe payment intent '%s' is not succeeded (status=%s).".formatted(transactionId, status));
        }
    }
}
