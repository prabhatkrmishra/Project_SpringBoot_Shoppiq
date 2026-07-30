package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.config.PaymentGatewayProperties;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.general.payment.PaymentGatewayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

/**
 * Concrete payment gateway strategy for UPI (Unified Payments Interface, India).
 *
 * <p>This gateway implements the UPI collect model. On {@link #process(Payment)},
 * it initiates a collect request against the customer's VPA (Virtual Payment
 * Address) through a Payment Service Provider (PSP). On
 * {@link #verify(Payment, String)}, it polls the PSP for the transaction
 * status to confirm whether the payment was completed. Authentication uses
 * the gateway's API credentials.</p>
 *
 * <p>UPI is a popular payment method in India that allows customers to pay
 * directly from their bank accounts. The gateway supports popular UPI apps
 * including Google Pay, PhonePe, Paytm, and BHIM. The merchant VPA is
 * configured through
 * {@link com.pkmprojects.shoppiq.config.GatewayConfig#merchantVpa}.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Slf4j
@Component
public final class UpiGateway extends AbstractRestGateway {

    private final String merchantVpa;

    /**
     * Constructs a UPI gateway with the given HTTP client, JSON mapper,
     * and gateway-specific configuration.
     *
     * @param restClientBuilder builder for {@link RestClient}
     * @param objectMapper      Jackson 3 {@link JsonMapper}
     * @param properties        typed payment gateway properties
     */
    public UpiGateway(RestClient.Builder restClientBuilder,
                      JsonMapper objectMapper,
                      PaymentGatewayProperties properties,
                      Clock clock) {
        super(restClientBuilder, objectMapper,
                properties.getUpi().getBaseUrl(),
                properties.getUpi().getApiKey(),
                properties.getUpi().getApiSecret() != null ? properties.getUpi().getApiSecret() : "",
                clock);
        this.merchantVpa = properties.getUpi().getMerchantVpa();
        if (properties.getUpi().getApiSecret() == null) {
            log.warn("UPI gateway 'api-secret' is not configured. UPI verification may fail.");
        }
    }

    /**
     * Returns the gateway type handled by this strategy.
     *
     * @return {@link PaymentGateway#UPI}
     */
    @Override
    public PaymentGateway supports() {
        return PaymentGateway.UPI;
    }

    /**
     * Returns the human-readable gateway name for error messages.
     *
     * @return {@code "UPI"}
     */
    @Override
    protected String gatewayName() {
        return "UPI";
    }

    /**
     * Initiates a UPI collect request for the given payment.
     *
     * <p>If a gateway payment ID already exists the payment is simply
     * marked {@code PROCESSING} (idempotent re-entry). Otherwise a
     * collect request is sent to the PSP's UPI endpoint.</p>
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
                "merchantVpa", merchantVpa == null ? "" : merchantVpa,
                "transactionRef", payment.getPaymentReference(),
                "note", "Shoppiq order " + payment.getPaymentReference()
        );

        String response = exchange(HttpMethod.POST, "/upi/collect", body, bearer(apiKey));
        String txnId = parse(response).get("txnId").asText();

        payment.setGatewayPaymentId(txnId);
        payment.setGateway(PaymentGateway.UPI);
        payment.setPaymentStatus(PaymentStatus.PROCESSING);
        payment.setGatewayResponse(response);
    }

    /**
     * Verifies a UPI collect payment by polling the PSP for the
     * transaction status.
     *
     * @param payment       the payment to verify
     * @param transactionId the PSP transaction ID
     * @throws PaymentGatewayException if the collect status is not
     *                                 {@code SUCCESS} or {@code CREDITED}
     */
    @Override
    public void verify(Payment payment, String transactionId) {
        String sanitized = sanitizeTransactionId(transactionId);
        String response = exchange(HttpMethod.GET, "/upi/status/" + sanitized, null, bearer(apiKey));
        String status = parse(response).get("status").asText();

        if ("SUCCESS".equals(status) || "CREDITED".equals(status)) {
            payment.setTransactionId(transactionId);
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(Instant.now(clock));
            payment.setGatewayResponse(response);
        } else {
            throw new PaymentGatewayException(
                    "UPI collect '%s' is not successful (status=%s).".formatted(transactionId, status));
        }
    }
}
