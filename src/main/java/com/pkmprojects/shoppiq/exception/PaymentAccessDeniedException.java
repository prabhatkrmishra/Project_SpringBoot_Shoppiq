package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a user tries to access a payment that does not belong to them.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class PaymentAccessDeniedException extends UnauthorizedOperationException {

    public PaymentAccessDeniedException(String detail) {
        super(ErrorCode.PAYMENT_ACCESS_DENIED, detail);
    }

    public static PaymentAccessDeniedException forPayment(Long paymentId) {
        return new PaymentAccessDeniedException(
                "Payment with id '%d' does not belong to you.".formatted(paymentId));
    }
}
