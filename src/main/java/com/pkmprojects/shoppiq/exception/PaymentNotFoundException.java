package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a requested payment record cannot be found.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class PaymentNotFoundException extends ResourceNotFoundException {

    public PaymentNotFoundException(String detail) {
        super(ErrorCode.PAYMENT_NOT_FOUND, detail);
    }

    public static PaymentNotFoundException forId(Long id) {
        return new PaymentNotFoundException(
                "Payment with id '%d' was not found.".formatted(id));
    }

    public static PaymentNotFoundException forTransactionId(String transactionId) {
        return new PaymentNotFoundException(
                "No payment found for transaction id '%s'.".formatted(transactionId));
    }

    public static PaymentNotFoundException forOrder(Long orderId) {
        return new PaymentNotFoundException(
                "No payment found for order id '%d'.".formatted(orderId));
    }
}
