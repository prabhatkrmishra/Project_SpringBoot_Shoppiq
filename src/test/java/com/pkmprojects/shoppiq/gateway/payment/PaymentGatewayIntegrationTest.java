package com.pkmprojects.shoppiq.gateway.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.pkmprojects.shoppiq.config.PaymentGatewayProperties;
import com.pkmprojects.shoppiq.entity.Payment;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.PaymentGatewayException;
import org.junit.jupiter.api.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the REST-backed payment gateway strategies.
 *
 * <p>Each gateway is exercised against a {@link WireMockServer} that mimics the
 * provider's sandbox API, validating request shape, status transitions, and
 * error handling (upstream 5xx → {@link PaymentGatewayException}).</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Payment Gateway Integration Tests")
class PaymentGatewayIntegrationTest {

    private WireMockServer wireMock;
    private ObjectMapper objectMapper;

    @BeforeAll
    void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
        objectMapper = new ObjectMapper();
    }

    @AfterAll
    void stopWireMock() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    @BeforeEach
    void reset() {
        wireMock.resetAll();
    }

    // ────────────────────────────────────────────────────────────
    // Razorpay
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Razorpay: process creates order, verify marks PAID on capture")
    void razorpay_processAndVerify() {
        wireMock.stubFor(post(urlEqualTo("/orders"))
                .willReturn(okJson("{\"id\":\"order_1\",\"status\":\"created\"}")));
        wireMock.stubFor(get(urlEqualTo("/payments/txn_1"))
                .willReturn(okJson("{\"id\":\"txn_1\",\"status\":\"captured\"}")));

        RazorpayGateway gateway = razorpayGateway();
        Payment payment = samplePayment(PaymentMethod.ONLINE);

        gateway.process(payment);
        assertThat(payment.getGatewayPaymentId()).isEqualTo("order_1");
        assertThat(payment.getGateway()).isEqualTo(PaymentGateway.RAZORPAY);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PROCESSING);

        gateway.verify(payment, "txn_1");
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getTransactionId()).isEqualTo("txn_1");
        assertThat(payment.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("Razorpay: upstream 5xx throws PaymentGatewayException")
    void razorpay_upstreamError() {
        wireMock.stubFor(post(urlEqualTo("/orders"))
                .willReturn(aResponse().withStatus(500).withBody("boom")));
        RazorpayGateway gateway = razorpayGateway();

        assertThatThrownBy(() -> gateway.process(samplePayment(PaymentMethod.ONLINE)))
                .isInstanceOf(PaymentGatewayException.class);
    }

    // ────────────────────────────────────────────────────────────
    // Stripe
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Stripe: process creates intent, verify marks PAID on success")
    void stripe_processAndVerify() {
        wireMock.stubFor(post(urlEqualTo("/payment_intents"))
                .willReturn(okJson("{\"id\":\"pi_1\",\"status\":\"requires_payment_method\"}")));
        wireMock.stubFor(get(urlEqualTo("/payment_intents/pi_1"))
                .willReturn(okJson("{\"id\":\"pi_1\",\"status\":\"succeeded\"}")));

        StripeGateway gateway = stripeGateway();
        Payment payment = samplePayment(PaymentMethod.STRIPE);

        gateway.process(payment);
        assertThat(payment.getGatewayPaymentId()).isEqualTo("pi_1");
        assertThat(payment.getGateway()).isEqualTo(PaymentGateway.STRIPE);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PROCESSING);

        gateway.verify(payment, "pi_1");
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    // ────────────────────────────────────────────────────────────
    // PayPal
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PayPal: token + order + capture flow marks PAID")
    void paypal_processAndVerify() {
        wireMock.stubFor(post(urlEqualTo("/v1/oauth2/token"))
                .willReturn(okJson("{\"access_token\":\"TOK\",\"expires_in\":3600}")));
        wireMock.stubFor(post(urlEqualTo("/v2/checkout/orders"))
                .willReturn(okJson("{\"id\":\"ORDER_1\",\"status\":\"CREATED\"}")));
        wireMock.stubFor(post(urlEqualTo("/v2/checkout/orders/ORDER_1/capture"))
                .willReturn(okJson("{\"id\":\"ORDER_1\",\"status\":\"COMPLETED\"}")));

        PaypalGateway gateway = paypalGateway();
        Payment payment = samplePayment(PaymentMethod.PAYPAL);

        gateway.process(payment);
        assertThat(payment.getGatewayPaymentId()).isEqualTo("ORDER_1");
        assertThat(payment.getGateway()).isEqualTo(PaymentGateway.PAYPAL);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PROCESSING);

        gateway.verify(payment, "ORDER_1");
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    // ────────────────────────────────────────────────────────────
    // UPI
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UPI: collect then status SUCCESS marks PAID")
    void upi_processAndVerify() {
        wireMock.stubFor(post(urlEqualTo("/upi/collect"))
                .willReturn(okJson("{\"txnId\":\"UPI_1\",\"status\":\"PENDING\"}")));
        wireMock.stubFor(get(urlEqualTo("/upi/status/UPI_1"))
                .willReturn(okJson("{\"txnId\":\"UPI_1\",\"status\":\"SUCCESS\"}")));

        UpiGateway gateway = upiGateway();
        Payment payment = samplePayment(PaymentMethod.UPI);

        gateway.process(payment);
        assertThat(payment.getGatewayPaymentId()).isEqualTo("UPI_1");
        assertThat(payment.getGateway()).isEqualTo(PaymentGateway.UPI);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PROCESSING);

        gateway.verify(payment, "UPI_1");
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    // ────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────

    private RazorpayGateway razorpayGateway() {
        PaymentGatewayProperties props = new PaymentGatewayProperties();
        props.getRazorpay().setBaseUrl(wireMock.baseUrl());
        props.getRazorpay().setApiKey("rzp_key");
        props.getRazorpay().setApiSecret("rzp_secret");
        return new RazorpayGateway(http11Client(), objectMapper, props);
    }

    private StripeGateway stripeGateway() {
        PaymentGatewayProperties props = new PaymentGatewayProperties();
        props.getStripe().setBaseUrl(wireMock.baseUrl());
        props.getStripe().setApiKey("sk_test");
        return new StripeGateway(http11Client(), objectMapper, props);
    }

    private PaypalGateway paypalGateway() {
        PaymentGatewayProperties props = new PaymentGatewayProperties();
        props.getPaypal().setBaseUrl(wireMock.baseUrl());
        props.getPaypal().setApiKey("client");
        props.getPaypal().setApiSecret("secret");
        return new PaypalGateway(http11Client(), objectMapper, props);
    }

    private UpiGateway upiGateway() {
        PaymentGatewayProperties props = new PaymentGatewayProperties();
        props.getUpi().setBaseUrl(wireMock.baseUrl());
        props.getUpi().setApiKey("upi_key");
        props.getUpi().setMerchantVpa("shoppiq@bank");
        return new UpiGateway(http11Client(), objectMapper, props);
    }

    private static RestClient.Builder http11Client() {
        return RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory());
    }

    private Payment samplePayment(PaymentMethod method) {
        return Payment.builder()
                .paymentReference("PAY-20260708-1")
                .paymentMethod(method)
                .paymentStatus(PaymentStatus.PENDING)
                .gateway(PaymentGateway.NONE)
                .amount(BigDecimal.valueOf(500))
                .currency("INR")
                .build();
    }
}
