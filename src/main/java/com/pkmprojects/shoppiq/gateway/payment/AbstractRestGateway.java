package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.exception.general.payment.PaymentGatewayException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * <strong>Spring Boot Concept:</strong> Abstract base class in the
 * {@code gateway.payment} package that implements the
 * {@link PaymentGatewayStrategy} interface using a <em>Template Method</em>
 * pattern for REST-based payment integrations.
 *
 * <p>Provides common HTTP exchange infrastructure shared by concrete
 * gateway implementations (Razorpay, Stripe, PayPal, UPI). Handles
 * request construction, authentication header injection, response
 * parsing, and error translation into {@link PaymentGatewayException}.</p>
 *
 * <p><strong>Educational value:</strong> This class demonstrates several
 * patterns and Spring Boot concepts:
 * <ul>
 *   <li><strong>Template Method</strong> — the {@link #exchange} and
 *       {@link #exchangeForm} methods define the skeleton of an HTTP call
 *       (URL expansion, auth headers, error handling), while subclasses
 *       provide the specific authentication via {@code Consumer<HttpHeaders>}
 *       lambdas passed to these methods.</li>
 *   <li><strong>Strategy + Abstract Base Class</strong> — {@code PaymentGatewayStrategy}
 *       defines the contract; this abstract class provides reusable REST
 *       infrastructure; concrete subclasses ({@link RazorpayGateway},
 *       {@link StripeGateway}, etc.) implement only the gateway-specific
 *       logic (endpoints, payload, auth scheme).</li>
 *   <li><strong>Spring {@link RestClient}</strong> — the idiomatic Spring
 *       HTTP client replacing the older {@code RestTemplate}. Note the
 *       fluent builder API and the use of
 *       {@code onStatus} for custom error handling.</li>
 *   <li><strong>Utility methods</strong> — {@link #toMinorUnits} converts
 *       {@link java.math.BigDecimal} amounts to the minor-unit format
 *       (paise/cents) expected by most payment gateways.</li>
 * </ul>
 * </p>
 *
 * <p>Subclasses implement {@link #gatewayName()} and define their
 * specific authentication and signature schemes.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
abstract non-sealed class AbstractRestGateway implements PaymentGatewayStrategy {

    protected final RestClient restClient;
    protected final JsonMapper objectMapper;
    protected final String baseUrl;
    protected final String apiKey;
    protected final String apiSecret;

    /**
     * Constructs the base gateway with a shared REST client, JSON mapper,
     * and provider-specific credentials.
     *
     * @param restClientBuilder builder for {@link RestClient}
     * @param objectMapper      Jackson 3 {@link JsonMapper}
     * @param baseUrl           the gateway's base URL
     * @param apiKey            the API key / client ID
     * @param apiSecret         the API secret / client secret
     */
    protected AbstractRestGateway(RestClient.Builder restClientBuilder,
                                  JsonMapper objectMapper,
                                  String baseUrl,
                                  String apiKey,
                                  String apiSecret) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    /**
     * Returns the human-readable gateway name for use in error messages
     * and logging.
     *
     * @return gateway display name
     */
    protected abstract String gatewayName();

    /**
     * Performs an HTTP JSON exchange against the gateway.
     *
     * @param method the HTTP method
     * @param path   the request path (appended to the base URL)
     * @param body   the request body (serialised as JSON), may be {@code null}
     * @param auth   a consumer that sets authentication headers
     * @return the response body as a string
     * @throws PaymentGatewayException if the gateway returns an error status
     */
    protected String exchange(HttpMethod method, String path, Object body, Consumer<HttpHeaders> auth) {
        try {
            RestClient.RequestBodySpec spec = restClient.method(method)
                    .uri(expand(path))
                    .headers(auth);
            if (body != null) {
                spec = spec.body(body);
            }
            return applyErrorHandling(spec.retrieve()).body(String.class);
        } catch (PaymentGatewayException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw PaymentGatewayException.of(gatewayName(), ex);
        }
    }

    /**
     * Performs an HTTP form-URL-encoded exchange against the gateway.
     *
     * @param method   the HTTP method
     * @param path     the request path (appended to the base URL)
     * @param formBody the URL-encoded form body
     * @param auth     a consumer that sets authentication headers
     * @return the response body as a string
     * @throws PaymentGatewayException if the gateway returns an error status
     */
    protected String exchangeForm(HttpMethod method, String path, String formBody, Consumer<HttpHeaders> auth) {
        try {
            return applyErrorHandling(restClient.method(method)
                    .uri(expand(path))
                    .headers(auth)
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formBody)
                    .retrieve()).body(String.class);
        } catch (PaymentGatewayException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw PaymentGatewayException.of(gatewayName(), ex);
        }
    }

    private RestClient.ResponseSpec applyErrorHandling(RestClient.ResponseSpec spec) {
        return spec.onStatus(status -> status.isError(), (request, response) -> {
            byte[] bytes = response.getBody().readAllBytes();
            String body = new String(bytes, StandardCharsets.UTF_8);
            throw PaymentGatewayException.ofResponse(
                    gatewayName(), response.getStatusCode().value(), body);
        });
    }

    /**
     * Parses a JSON response body into a {@link JsonNode}.
     *
     * @param body the raw JSON string
     * @return the parsed JSON tree
     * @throws PaymentGatewayException if parsing fails
     */
    protected JsonNode parse(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception ex) {
            throw PaymentGatewayException.of(gatewayName(), ex);
        }
    }

    /**
     * Expands a relative path against the configured base URL.
     *
     * @param path the relative path
     * @return the fully qualified URL string
     */
    protected String expand(String path) {
        return baseUrl.replaceAll("/+$", "") + path;
    }

    /**
     * Validates that a transaction ID contains only safe characters and
     * cannot be used for URL path traversal.
     *
     * <p>Payment gateway transaction IDs are typically alphanumeric with
     * underscores and hyphens (e.g. {@code pay_ABC123xyz},
     * {@code pi_1234567890}). This method rejects any value containing
     * characters that could manipulate the URL path ({@code /},
     * {@code \}, {@code ..}, {@code %2f}, {@code %2e}, etc.).</p>
     *
     * @param transactionId the transaction ID to validate
     * @return the validated transaction ID
     * @throws PaymentGatewayException if the transaction ID contains invalid characters
     */
    protected String sanitizeTransactionId(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new PaymentGatewayException("Transaction ID must not be blank.");
        }
        if (!transactionId.matches("[a-zA-Z0-9_\\-]+")) {
            throw new PaymentGatewayException(
                    "%s transaction ID contains invalid characters: '%s'".formatted(gatewayName(), transactionId));
        }
        return transactionId;
    }

    /**
     * Converts a {@link BigDecimal} amount to minor units (e.g. paise, cents).
     *
     * @param amount the amount in major units
     * @return the amount in minor units
     */
    protected long toMinorUnits(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, java.math.RoundingMode.HALF_UP).longValue();
    }

    /**
     * Returns an HTTP Basic authentication header consumer.
     *
     * @param user   the username (API key)
     * @param secret the password (API secret)
     * @return a header consumer
     */
    protected Consumer<HttpHeaders> basicAuth(String user, String secret) {
        return headers -> headers.setBasicAuth(user, secret);
    }

    /**
     * Returns a Bearer token authentication header consumer.
     *
     * @param token the bearer token
     * @return a header consumer
     */
    protected Consumer<HttpHeaders> bearer(String token) {
        return headers -> headers.setBearerAuth(token);
    }
}
