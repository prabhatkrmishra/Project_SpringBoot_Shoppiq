package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when an operation cannot be performed because the payment
 * is in an incompatible state.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class PaymentInvalidStateException extends InvalidOperationException {

    public PaymentInvalidStateException(String detail) {
        super(ErrorCode.PAYMENT_INVALID_STATE, detail);
    }

    public static PaymentInvalidStateException alreadyPaid(Long paymentId) {
        return new PaymentInvalidStateException(
                "Payment '%d' has already been paid.".formatted(paymentId));
    }

    public static PaymentInvalidStateException cannotPay(Long paymentId, PaymentStatus current) {
        return new PaymentInvalidStateException(
                "Payment '%d' cannot be processed — current status: '%s'."
                        .formatted(paymentId, current));
    }

    public static PaymentInvalidStateException cannotCancel(Long paymentId, PaymentStatus current) {
        return new PaymentInvalidStateException(
                "Payment '%d' cannot be cancelled — current status: '%s'."
                        .formatted(paymentId, current));
    }

    public static PaymentInvalidStateException refundNotAllowed(Long paymentId, PaymentStatus current) {
        return new PaymentInvalidStateException(
                "Refund not allowed for payment '%d' in status '%s'. Only PAID payments can be refunded."
                        .formatted(paymentId, current));
    }
}
