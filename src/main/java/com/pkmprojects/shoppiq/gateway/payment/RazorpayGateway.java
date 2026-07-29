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
 * for Razorpay (India, INR), extending {@link AbstractRestGateway}.
 *
 * <p>Flow: {@link #process(Payment)} creates a Razorpay <em>order</em>
 * ({@code POST /v1/orders}); {@link #verify(Payment, String)} fetches the
 * payment by its gateway id ({@code GET /v1/payments/{id}}) and marks the
 * payment {@code PAID} once it is {@code captured} (or {@code authorized}).</p>
 *
 * <p>Verification is performed server-to-server via the Razorpay API using
 * the configured API key and secret in every environment. This approach
 * confirms the payment capture status directly with the gateway and does
 * not rely on webhook signature verification, which applies only when
 * accepting asynchronous webhook callbacks.</p>
 *
 * <p><strong>Educational value:</strong> Contrast with the other gateway
 * implementations:
 * <ul>
 *   <li><strong>Basic auth vs bearer</strong> — Razorpay uses HTTP Basic
 *       auth (API key:secret) unlike PayPal and Stripe which use bearer
 *       tokens. This is configured by passing {@code basicAuth(apiKey, apiSecret)}
 *       to the exchange method.</li>
 *   <li><strong>Server-to-server verification</strong> — unlike webhook-based
 *       approaches, Razorpay verification happens via a direct GET request
 *       to the gateway after the client completes payment on the frontend.</li>
 *   <li><strong>Minor unit conversion</strong> — uses
 *       {@link AbstractRestGateway#toMinorUnits} to convert the amount to
 *       paise (Razorpay's expected format).</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Component
public final class RazorpayGateway extends AbstractRestGateway {

    /**
     * Constructs a Razorpay gateway with the given HTTP client, JSON mapper,
     * and gateway-specific configuration.
     *
     * @param restClientBuilder builder for {@link RestClient}
     * @param objectMapper      Jackson 3 {@link JsonMapper}
     * @param properties        typed payment gateway properties
     */
    public RazorpayGateway(RestClient.Builder restClientBuilder,
                           JsonMapper objectMapper,
                           PaymentGatewayProperties properties,
                           Clock clock) {
        super(restClientBuilder, objectMapper,
                properties.getRazorpay().getBaseUrl(),
                properties.getRazorpay().getApiKey(),
                properties.getRazorpay().getApiSecret(),
                clock);
    }

    /**
     * Returns the gateway type handled by this strategy.
     *
     * @return {@link PaymentGateway#RAZORPAY}
     */
    @Override
    public PaymentGateway supports() {
        return PaymentGateway.RAZORPAY;
    }

    /**
     * Returns the human-readable gateway name for error messages.
     *
     * @return {@code "Razorpay"}
     */
    @Override
    protected String gatewayName() {
        return "Razorpay";
    }

    /**
     * Creates a Razorpay order for the given payment.
     *
     * <p>If a gateway order ID already exists the payment is simply
     * marked {@code PROCESSING} (idempotent re-entry).</p>
     *
     * @param payment the payment to process
     */
    @Override
    public void process(Payment payment) {
        // Idempotent: a gateway order already exists for this payment.
        if (payment.getGatewayPaymentId() != null) {
            payment.setPaymentStatus(PaymentStatus.PROCESSING);
            return;
        }

        Map<String, Object> body = Map.of(
                "amount", toMinorUnits(payment.getAmount()),
                "currency", payment.getCurrency(),
                "receipt", payment.getPaymentReference(),
                "payment_capture", 1
        );

        String response = exchange(HttpMethod.POST, "/orders", body,
                basicAuth(apiKey, apiSecret));
        String orderId = parse(response).get("id").asText();

        payment.setGatewayPaymentId(orderId);
        payment.setGateway(PaymentGateway.RAZORPAY);
        payment.setPaymentStatus(PaymentStatus.PROCESSING);
        payment.setGatewayResponse(response);
    }

    /**
     * Verifies a Razorpay payment by fetching its capture status from
     * the Razorpay API.
     *
     * @param payment       the payment to verify
     * @param transactionId the Razorpay payment ID
     * @throws PaymentGatewayException if the payment status is not
     *                                 {@code captured} or {@code authorized}
     */
    @Override
    public void verify(Payment payment, String transactionId) {
        String sanitized = sanitizeTransactionId(transactionId);
        String response = exchange(HttpMethod.GET, "/payments/" + sanitized, null,
                basicAuth(apiKey, apiSecret));
        String status = parse(response).get("status").asText();

        if ("captured".equals(status) || "authorized".equals(status)) {
            payment.setTransactionId(transactionId);
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(Instant.now(clock));
            payment.setGatewayResponse(response);
        } else {
            throw new PaymentGatewayException(
                    "Razorpay payment '%s' is not captured (status=%s).".formatted(transactionId, status));
        }
    }
}
