package com.pkmprojects.shoppiq.config;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the startup-time payment gateway URL validation in
 * {@link PaymentGatewayProperties#validateGatewayUrls()}.
 *
 * <p>Verifies that enabled gateways pointing to internal addresses
 * (loopback, RFC 1918, link-local, cloud metadata) are rejected at
 * startup, while external URLs and disabled gateways pass validation.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
@DisplayName("PaymentGatewayProperties SSRF URL Validation Tests")
class PaymentGatewayPropertiesValidationTest {

    // ────────────────────────────────────────────────────────────
    // Internal URLs should be rejected
    // ────────────────────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {
            "http://127.0.0.1:8080/api/v1",
            "http://localhost:3000/payments",
            "http://0.0.0.0:443/api",
            "http://10.0.0.1:8080",
            "http://172.16.0.1:8080",
            "http://192.168.1.1:8080",
            "http://169.254.169.254/latest/meta-data/",
            "http://169.254.0.1:80/api"
    })
    @DisplayName("Rejects enabled gateway with internal base URL")
    void rejectsInternalBaseUrl(String url) {
        PaymentGatewayProperties props = new PaymentGatewayProperties(new GatewayConfig(), new GatewayConfig(), new GatewayConfig(), new GatewayConfig());
        props.getRazorpay().setBaseUrl(url);
        props.getRazorpay().setEnabled(true);
        props.getRazorpay().setApiKey("test-key");
        props.getRazorpay().setApiSecret("test-secret");

        assertThatThrownBy(props::validateGatewayUrls)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("razorpay")
                .hasMessageContaining("internal address");
    }

    @Test
    @DisplayName("Rejects internal URL even if other gateways are valid")
    void rejectsInternalAmongValid() {
        PaymentGatewayProperties props = new PaymentGatewayProperties(new GatewayConfig(), new GatewayConfig(), new GatewayConfig(), new GatewayConfig());

        // Razorpay: internal URL
        props.getRazorpay().setBaseUrl("http://169.254.169.254/api");
        props.getRazorpay().setEnabled(true);
        props.getRazorpay().setApiKey("key");
        props.getRazorpay().setApiSecret("secret");

        // Stripe: valid external URL
        props.getStripe().setBaseUrl("https://api.stripe.com/v1");
        props.getStripe().setEnabled(true);
        props.getStripe().setApiKey("key");
        props.getStripe().setApiSecret("secret");

        assertThatThrownBy(props::validateGatewayUrls)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("razorpay");
    }

    @Test
    @DisplayName("Rejects gateway with empty host in URL")
    void rejectsEmptyHost() {
        PaymentGatewayProperties props = new PaymentGatewayProperties(new GatewayConfig(), new GatewayConfig(), new GatewayConfig(), new GatewayConfig());
        props.getRazorpay().setBaseUrl("http:///api");
        props.getRazorpay().setEnabled(true);
        props.getRazorpay().setApiKey("key");
        props.getRazorpay().setApiSecret("secret");

        assertThatThrownBy(props::validateGatewayUrls)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no valid host");
    }

    // ────────────────────────────────────────────────────────────
    // External URLs should be accepted
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Accepts enabled gateway with external base URL")
    void acceptsExternalBaseUrl() {
        PaymentGatewayProperties props = new PaymentGatewayProperties(new GatewayConfig(), new GatewayConfig(), new GatewayConfig(), new GatewayConfig());
        props.getRazorpay().setBaseUrl("https://api.razorpay.com/v1");
        props.getRazorpay().setEnabled(true);
        props.getRazorpay().setApiKey("test-key");
        props.getRazorpay().setApiSecret("test-secret");

        assertThatCode(props::validateGatewayUrls)
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Accepts all gateways with external URLs")
    void acceptsAllExternalUrls() {
        PaymentGatewayProperties props = new PaymentGatewayProperties(new GatewayConfig(), new GatewayConfig(), new GatewayConfig(), new GatewayConfig());
        props.getRazorpay().setBaseUrl("https://api.razorpay.com/v1");
        props.getRazorpay().setEnabled(true);
        props.getRazorpay().setApiKey("key");
        props.getRazorpay().setApiSecret("secret");

        props.getStripe().setBaseUrl("https://api.stripe.com/v1");
        props.getStripe().setEnabled(true);
        props.getStripe().setApiKey("key");
        props.getStripe().setApiSecret("secret");

        props.getPaypal().setBaseUrl("https://api-m.sandbox.paypal.com");
        props.getPaypal().setEnabled(true);
        props.getPaypal().setApiKey("key");
        props.getPaypal().setApiSecret("secret");

        assertThatCode(props::validateGatewayUrls)
                .doesNotThrowAnyException();
    }

    // ────────────────────────────────────────────────────────────
    // Disabled gateways should be skipped
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Skips disabled gateways even with internal URLs")
    void skipsDisabledGateways() {
        PaymentGatewayProperties props = new PaymentGatewayProperties(new GatewayConfig(), new GatewayConfig(), new GatewayConfig(), new GatewayConfig());
        props.getRazorpay().setBaseUrl("http://127.0.0.1:8080/api");
        props.getRazorpay().setEnabled(false);

        props.getStripe().setBaseUrl("http://192.168.1.1/api");
        props.getStripe().setEnabled(false);

        assertThatCode(props::validateGatewayUrls)
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Skips gateways with no base URL configured")
    void skipsUnconfiguredGateways() {
        PaymentGatewayProperties props = new PaymentGatewayProperties(new GatewayConfig(), new GatewayConfig(), new GatewayConfig(), new GatewayConfig());
        // All gateways default to enabled=false, baseUrl=null
        // Should not throw
        assertThatCode(props::validateGatewayUrls)
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Skips enabled gateway with blank base URL")
    void skipsBlankBaseUrl() {
        PaymentGatewayProperties props = new PaymentGatewayProperties(new GatewayConfig(), new GatewayConfig(), new GatewayConfig(), new GatewayConfig());
        props.getRazorpay().setBaseUrl("   ");
        props.getRazorpay().setEnabled(true);

        assertThatCode(props::validateGatewayUrls)
                .doesNotThrowAnyException();
    }

    // ────────────────────────────────────────────────────────────
    // Edge cases
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Handles unresolvable hostname gracefully")
    void handlesUnresolvableHostname() {
        PaymentGatewayProperties props = new PaymentGatewayProperties(
                new GatewayConfig(), new GatewayConfig(), new GatewayConfig(), new GatewayConfig());
        // This hostname won't resolve but shouldn't crash startup
        props.getRazorpay().setBaseUrl("https://this-host-does-not-exist-12345.invalid/api");
        props.getRazorpay().setEnabled(true);

        // Should log a warning, not throw
        assertThatCode(props::validateGatewayUrls)
                .doesNotThrowAnyException();
    }
}
