package com.pkmprojects.shoppiq.exception.general.payment;

import com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a user tries
 * to access a payment that does not belong to them.
 *
 * <p>Leaf exception in the authorization hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException}
 * (HTTP 403) for payment ownership violations.</p>
 *
 * @author prabhatkrmishra
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
