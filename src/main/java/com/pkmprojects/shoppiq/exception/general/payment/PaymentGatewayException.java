package com.pkmprojects.shoppiq.exception.general.payment;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when communication
 * with an external payment gateway fails.
 *
 * <p>Extends {@link com.pkmprojects.shoppiq.exception.base.ShoppiqException}
 * directly (not a business exception) because gateway errors are integration
 * failures. Uses HTTP 502 (Bad Gateway) to attribute the failure to the
 * upstream provider. Provides factory methods for wrapping a
 * {@link Throwable} with cause chaining, and for capturing the gateway's
 * HTTP status code and response body for diagnostics.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class PaymentGatewayException extends ShoppiqException {

    /**
     * Creates a new payment gateway exception with a detail message.
     *
     * @param detail error description
     */
    public PaymentGatewayException(String detail) {
        super(ErrorCode.PAYMENT_GATEWAY_ERROR, HttpStatus.BAD_GATEWAY, detail);
    }

    /**
     * Creates a new payment gateway exception wrapping a cause.
     *
     * @param detail error description
     * @param cause  the underlying throwable
     */
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
