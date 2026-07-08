package com.pkmprojects.shoppiq.gateway.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkmprojects.shoppiq.config.PaymentGatewayProperties;
import com.pkmprojects.shoppiq.entity.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.PaymentGatewayException;
import com.pkmprojects.shoppiq.exception.PaymentInvalidStateException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

/**
 * Razorpay payment gateway integration (India, INR).
 *
 * <p>Flow: {@link #process(Payment)} creates a Razorpay <em>order</em>
 * ({@code POST /v1/orders}); {@link #verify(Payment, String)} fetches the
 * payment by its gateway id ({@code GET /v1/payments/{id}}) and marks the
 * payment {@code PAID} once it is {@code captured} (or {@code authorized}).</p>
 *
 * <p>Production deployments should additionally verify the Razorpay signature
 * ({@code order_id|payment_id} with the webhook/secret) before marking paid;
 * this implementation confirms capture status directly with the gateway, which
 * is sufficient for sandbox/test usage.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Component
public class RazorpayGateway extends AbstractRestGateway {

    public RazorpayGateway(RestClient.Builder restClientBuilder,
                           ObjectMapper objectMapper,
                           PaymentGatewayProperties properties) {
        super(restClientBuilder, objectMapper,
                properties.getRazorpay().getBaseUrl(),
                properties.getRazorpay().getApiKey(),
                properties.getRazorpay().getApiSecret());
    }

    @Override
    public PaymentGateway supports() {
        return PaymentGateway.RAZORPAY;
    }

    @Override
    protected String gatewayName() {
        return "Razorpay";
    }

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

    @Override
    public void verify(Payment payment, String transactionId) {
        String response = exchange(HttpMethod.GET, "/payments/" + transactionId, null,
                basicAuth(apiKey, apiSecret));
        String status = parse(response).get("status").asText();

        if ("captured".equals(status) || "authorized".equals(status)) {
            payment.setTransactionId(transactionId);
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(Instant.now());
            payment.setGatewayResponse(response);
        } else {
            throw new PaymentGatewayException(
                    "Razorpay payment '%s' is not captured (status=%s).".formatted(transactionId, status));
        }
    }
}
