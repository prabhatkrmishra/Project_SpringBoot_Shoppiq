package com.pkmprojects.shoppiq.exception.general.payment;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a payment gateway's configuration is invalid or incomplete.
 *
 * <p>This exception is thrown at application startup when a payment
 * gateway's configuration is missing required fields, has invalid values,
 * or points to an internal address blocked by SSRF protection. It uses the
 * {@link ErrorCode#INVALID_PAYMENT_GATEWAY_CONFIG} code and HTTP 500
 * Internal Server Error status. The application fails to start to prevent
 * processing payments with misconfigured gateways.</p>
 *
 * <p>The detail message includes the gateway name and specific
 * configuration issue (e.g., "Payment gateway 'razorpay' base URL
 * 'http://localhost:8080' resolves to internal address '127.0.0.1'.")
 * to help the administrator resolve the configuration problem.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#INVALID_PAYMENT_GATEWAY_CONFIG
 * @since 1.0.0
 */
public final class InvalidPaymentGatewayConfigException extends InvalidOperationException {

    private InvalidPaymentGatewayConfigException(String detail) {
        super(ErrorCode.INVALID_PAYMENT_GATEWAY_CONFIG, detail);
    }

    /**
     * Creates an exception indicating that the gateway's base URL has
     * no valid host.
     *
     * @param name the gateway name
     * @param url  the configured base URL
     * @return invalid config exception
     */
    public static InvalidPaymentGatewayConfigException invalidHost(String name, String url) {
        return new InvalidPaymentGatewayConfigException(
                "Payment gateway '%s' base URL '%s' has no valid host".formatted(name, url)
        );
    }

    /**
     * Creates an exception indicating that the gateway's base URL
     * resolves to an internal (non-public) address, blocked by SSRF
     * protection.
     *
     * @param name    the gateway name
     * @param url     the configured base URL
     * @param address the resolved internal address
     * @return invalid config exception
     */
    public static InvalidPaymentGatewayConfigException internalAddress(String name, String url, String address) {
        return new InvalidPaymentGatewayConfigException(
                "Payment gateway '%s' base URL '%s' resolves to internal address '%s'. "
                        .formatted(name, url, address)
                        + "SSRF protection requires payment gateways to use external URLs only. "
                        + "Set the URL to a publicly-routable address or disable the gateway."
        );
    }
}
