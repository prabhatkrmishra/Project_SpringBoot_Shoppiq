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
 * UPI payment gateway integration (India) via a PSP (Razorpay / PhonePe /
 * Cashfree / …).
 *
 * <p>UPI has no single public API; this strategy implements the common
 * <em>collect</em> contract: {@link #process(Payment)} raises a collect
 * request against the customer VPA (provided by the PSP config), and
 * {@link #verify(Payment, String)} polls the transaction status. The payment
 * is marked {@code PAID} when the PSP reports the collect as {@code SUCCESS}
 * (or {@code CREDITED}).</p>
 *
 * <p>Back this with a concrete PSP by pointing {@code shoppiq.payment.gateways.upi.base-url}
 * at the provider's UPI endpoint and supplying its merchant key/VPA.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Component
public class UpiGateway extends AbstractRestGateway {

    private final String merchantVpa;

    public UpiGateway(RestClient.Builder restClientBuilder,
                      ObjectMapper objectMapper,
                      PaymentGatewayProperties properties) {
        super(restClientBuilder, objectMapper,
                properties.getUpi().getBaseUrl(),
                properties.getUpi().getApiKey(),
                properties.getUpi().getApiSecret());
        this.merchantVpa = properties.getUpi().getMerchantVpa();
    }

    @Override
    public PaymentGateway supports() {
        return PaymentGateway.UPI;
    }

    @Override
    protected String gatewayName() {
        return "UPI";
    }

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

    @Override
    public void verify(Payment payment, String transactionId) {
        String response = exchange(HttpMethod.GET, "/upi/status/" + transactionId, null, bearer(apiKey));
        String status = parse(response).get("status").asText();

        if ("SUCCESS".equals(status) || "CREDITED".equals(status)) {
            payment.setTransactionId(transactionId);
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(Instant.now());
            payment.setGatewayResponse(response);
        } else {
            throw new PaymentGatewayException(
                    "UPI collect '%s' is not successful (status=%s).".formatted(transactionId, status));
        }
    }
}
