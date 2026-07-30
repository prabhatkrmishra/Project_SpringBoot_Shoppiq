package com.pkmprojects.shoppiq.exception.general.payment;

import com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a user tries to access a payment that does not belong to them.
 *
 * <p>This exception is thrown when a customer attempts to access a payment
 * record that belongs to another user. Payment records are private and
 * restricted to the payer and administrators. It uses the
 * {@link ErrorCode#PAYMENT_ACCESS_DENIED} code and HTTP 403 Forbidden
 * status.</p>
 *
 * <p>The detail message includes the payment identifier (e.g.,
 * "Payment with id '42' does not belong to you.") to help the client
 * understand which payment was restricted. The client should ensure they
 * are accessing their own payment records.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#PAYMENT_ACCESS_DENIED
 * @since 1.0.0
 */
public final class PaymentAccessDeniedException extends UnauthorizedOperationException {

    private PaymentAccessDeniedException(String detail) {
        super(ErrorCode.PAYMENT_ACCESS_DENIED, detail);
    }

    /**
     * Creates an exception for a user accessing a payment they do not own.
     *
     * @param paymentId the payment ID
     * @return a new exception instance
     */
    public static PaymentAccessDeniedException forPayment(Long paymentId) {
        return new PaymentAccessDeniedException(
                "Payment with id '%d' does not belong to you.".formatted(paymentId));
    }
}
