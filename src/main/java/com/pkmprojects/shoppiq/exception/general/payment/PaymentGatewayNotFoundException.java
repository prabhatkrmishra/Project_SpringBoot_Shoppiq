package com.pkmprojects.shoppiq.exception.general.payment;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when no payment gateway is configured for the requested payment method.
 *
 * <p>This exception is thrown when a customer selects a payment method
 * that has no enabled gateway. It uses the
 * {@link ErrorCode#PAYMENT_GATEWAY_NOT_FOUND} code and HTTP 404 Not
 * Found status. The administrator must configure and enable the
 * corresponding payment gateway.</p>
 *
 * <p>The detail message includes the payment method name (e.g.,
 * "No payment gateway configured for method: UPI") to help the client
 * understand which method was unavailable. The client should choose a
 * different payment method.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#PAYMENT_GATEWAY_NOT_FOUND
 * @since 1.0.0
 */
public final class PaymentGatewayNotFoundException extends ResourceNotFoundException {

    private PaymentGatewayNotFoundException(String detail) {
        super(ErrorCode.PAYMENT_GATEWAY_NOT_FOUND, detail);
    }

    /**
     * Creates an exception indicating that no gateway is configured
     * for the given payment method.
     *
     * @param method the payment method name
     * @return payment gateway not found exception
     */
    public static PaymentGatewayNotFoundException forMethod(String method) {
        return new PaymentGatewayNotFoundException(
                "No payment gateway configured for method: %s".formatted(method)
        );
    }
}
