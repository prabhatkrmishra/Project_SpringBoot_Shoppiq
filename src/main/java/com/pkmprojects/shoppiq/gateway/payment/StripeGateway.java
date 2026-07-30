package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.config.PaymentGatewayProperties;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.general.payment.PaymentGatewayException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

/**
 * Concrete payment gateway strategy for Stripe (global).
 *
 * <p>This gateway implements the Stripe payment flow using the PaymentIntents
 * API. On {@link #process(Payment)}, it creates a PaymentIntent with the
 * specified amount and currency. On {@link #verify(Payment, String)}, it
 * retrieves the PaymentIntent to confirm its status. Authentication uses
 * Bearer token with the Stripe secret key.</p>
 *
 * <p>Stripe supports multiple currencies and is the primary gateway for
 * international payments. The gateway converts amounts from major units
 * to minor units (cents) before sending to the API. Error responses from
 * Stripe are translated into {@link PaymentGatewayException} instances
 * with descriptive messages.</p>
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
