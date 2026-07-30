package com.pkmprojects.shoppiq.gateway.payment;

import com.pkmprojects.shoppiq.exception.general.payment.PaymentGatewayException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.function.Consumer;

/**
 * Abstract base class for REST-based payment gateway integrations.
 *
 * <p>This class provides common HTTP exchange infrastructure shared by
 * concrete gateway implementations (Stripe, Razorpay, PayPal, UPI). It
 * handles request construction, authentication header injection, response
 * parsing, error translation, and input sanitization. Subclasses only
 * need to implement the {@link PaymentGatewayStrategy} methods and use
 * the provided exchange utilities.</p>
 *
 * <p>The base class enforces security by sanitizing transaction IDs to
 * prevent URL path traversal attacks. It also provides utility methods
 * for currency conversion (major to minor units), HTTP Basic and Bearer
 * authentication, and JSON response parsing. All HTTP errors from the
 * gateway are translated into {@link PaymentGatewayException} instances
 * with descriptive error messages.</p>
 *
 * @author prabhatkrmishra
 * @see PaymentGatewayStrategy
 * @since 1.0.0
 */
abstract non-sealed class AbstractRestGateway implements PaymentGatewayStrategy {

    protected final RestClient restClient;
    protected final JsonMapper objectMapper;
    protected final String baseUrl;
    protected final String apiKey;
    protected final String apiSecret;
    protected final Clock clock;

    /**
     * Constructs the base gateway with a shared REST client, JSON mapper,
     * provider-specific credentials, and a clock for deterministic time.
     *
     * @param restClientBuilder builder for {@link RestClient}
     * @param objectMapper      Jackson 3 {@link JsonMapper}
     * @param baseUrl           the gateway's base URL
     * @param apiKey            the API key / client ID
     * @param apiSecret         the API secret / client secret
     * @param clock             clock for deterministic timestamps
     */
    protected AbstractRestGateway(RestClient.Builder restClientBuilder,
                                  JsonMapper objectMapper,
                                  String baseUrl,
                                  String apiKey,
                                  String apiSecret,
                                  Clock clock) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.clock = clock;
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
     * <p>Most payment gateways use 100 as the minor-unit factor
     * (1 major unit = 100 minor units). Override or extend for currencies
     * with different scales.</p>
     *
     * @param amount the amount in major units
     * @return the amount in minor units
     */
    protected long toMinorUnits(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValue();
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
