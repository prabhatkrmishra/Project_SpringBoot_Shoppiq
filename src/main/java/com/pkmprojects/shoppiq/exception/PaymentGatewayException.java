package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when communication with an external payment gateway fails
 * (network error, non-2xx response, malformed payload, timeout, …).
 *
 * <p>Surfaced to clients as an HTTP {@code 502 Bad Gateway} so the failure is
 * clearly attributed to the upstream payment provider rather than the
 * application itself.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class PaymentGatewayException extends ShoppiqException {

    public PaymentGatewayException(String detail) {
        super(ErrorCode.PAYMENT_GATEWAY_ERROR, HttpStatus.BAD_GATEWAY, detail);
    }

    public PaymentGatewayException(String detail, Throwable cause) {
        super(ErrorCode.PAYMENT_GATEWAY_ERROR, HttpStatus.BAD_GATEWAY, detail);
        initCause(cause);
    }

    /**
     * Wraps an unexpected error from the gateway layer.
     *
     * @param gateway gateway name (for diagnostics)
     * @param cause   the underlying throwable
     * @return a {@link PaymentGatewayException}
     */
    public static PaymentGatewayException of(String gateway, Throwable cause) {
        return new PaymentGatewayException("Payment gateway '%s' request failed: %s"
                .formatted(gateway, cause.getMessage()), cause);
    }

    /**
     * Builds an exception for a non-success HTTP response from the gateway.
     *
     * @param gateway      gateway name
     * @param statusCode   HTTP status returned by the gateway
     * @param responseBody gateway response body (may be null)
     * @return a {@link PaymentGatewayException}
     */
    public static PaymentGatewayException ofResponse(String gateway, int statusCode, String responseBody) {
        String body = responseBody == null ? "<no body>" : responseBody;
        return new PaymentGatewayException("Payment gateway '%s' returned HTTP %d: %s"
                .formatted(gateway, statusCode, body));
    }
}
