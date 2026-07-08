package com.pkmprojects.shoppiq.gateway.payment;

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

/**
 * Stripe payment gateway integration (global).
 *
 * <p>Flow: {@link #process(Payment)} creates a PaymentIntent
 * ({@code POST /v1/payment_intents}); {@link #verify(Payment, String)} fetches
 * the intent by id ({@code GET /v1/payment_intents/{id}}) and marks the payment
 * {@code PAID} once its status is {@code succeeded}.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Component
public class StripeGateway extends AbstractRestGateway {

    public StripeGateway(RestClient.Builder restClientBuilder,
                         ObjectMapper objectMapper,
                         PaymentGatewayProperties properties) {
        super(restClientBuilder, objectMapper,
                properties.getStripe().getBaseUrl(),
                properties.getStripe().getApiKey(),
                properties.getStripe().getApiSecret());
    }

    @Override
    public PaymentGateway supports() {
        return PaymentGateway.STRIPE;
    }

    @Override
    protected String gatewayName() {
        return "Stripe";
    }

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
        com.fasterxml.jackson.databind.JsonNode node = parse(response);
        String intentId = node.get("id").asText();
        String status = node.get("status").asText();

        payment.setGatewayPaymentId(intentId);
        payment.setGateway(PaymentGateway.STRIPE);
        payment.setPaymentStatus("succeeded".equals(status) ? PaymentStatus.PAID : PaymentStatus.PROCESSING);
        payment.setGatewayResponse(response);
    }

    @Override
    public void verify(Payment payment, String transactionId) {
        String response = exchange(HttpMethod.GET, "/payment_intents/" + transactionId, null, bearer(apiKey));
        String status = parse(response).get("status").asText();

        if ("succeeded".equals(status)) {
            payment.setTransactionId(transactionId);
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(Instant.now());
            payment.setGatewayResponse(response);
        } else {
            throw new PaymentGatewayException(
                    "Stripe payment intent '%s' is not succeeded (status=%s).".formatted(transactionId, status));
        }
    }
}
