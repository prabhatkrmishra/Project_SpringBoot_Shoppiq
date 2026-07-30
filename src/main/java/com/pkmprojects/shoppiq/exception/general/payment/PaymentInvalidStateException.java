package com.pkmprojects.shoppiq.exception.general.payment;

import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when an operation cannot be performed because the payment is in an incompatible state.
 *
 * <p>This exception is thrown when an operation is attempted on a payment
 * that is not in the correct state. For example, attempting to capture a
 * payment that is still in PENDING status, or attempting to refund a
 * payment that has not been paid. It uses the
 * {@link ErrorCode#PAYMENT_INVALID_STATE} code and HTTP 400 Bad Request
 * status.</p>
 *
 * <p>The detail message includes the payment ID and current status (e.g.,
 * "Payment '42' cannot be processed — current status: 'PENDING'.") to
 * help the client understand which state transition was invalid. The
 * client should verify the payment status before retrying.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#PAYMENT_INVALID_STATE
 * @since 1.0.0
 */
public final class PaymentInvalidStateException extends InvalidOperationException {

    private PaymentInvalidStateException(String detail) {
        super(ErrorCode.PAYMENT_INVALID_STATE, detail);
    }

    /**
     * Creates an exception for a payment that has already been paid.
     *
     * @param paymentId the payment ID
     * @return a new exception instance
     */
    public static PaymentInvalidStateException alreadyPaid(Long paymentId) {
        return new PaymentInvalidStateException(
                "Payment '%d' has already been paid."
                        .formatted(paymentId));
    }

    /**
     * Creates an exception for a payment that cannot be processed.
     *
     * @param paymentId the payment ID
     * @param current   the current payment status
     * @return a new exception instance
     */
    public static PaymentInvalidStateException cannotPay(Long paymentId, PaymentStatus current) {
        return new PaymentInvalidStateException(
                "Payment '%d' cannot be processed — current status: '%s'."
                        .formatted(paymentId, current));
    }

    /**
     * Creates an exception for a payment that cannot be cancelled.
     *
     * @param paymentId the payment ID
     * @param current   the current payment status
     * @return a new exception instance
     */
    public static PaymentInvalidStateException cannotCancel(Long paymentId, PaymentStatus current) {
        return new PaymentInvalidStateException(
                "Payment '%d' cannot be cancelled — current status: '%s'."
                        .formatted(paymentId, current));
    }

    /**
     * Creates an exception for a payment that cannot be verified.
     *
     * @param paymentId the payment ID
     * @param current   the current payment status
     * @return a new exception instance
     */
    public static PaymentInvalidStateException cannotVerify(Long paymentId, PaymentStatus current) {
        return new PaymentInvalidStateException(
                "Payment '%d' cannot be verified — current status: '%s'. Online payments must be in PROCESSING state to be verified."
                        .formatted(paymentId, current));
    }

    /**
     * Creates an exception for a payment that cannot be refunded.
     *
     * @param paymentId the payment ID
     * @param current   the current payment status
     * @return a new exception instance
     */
    public static PaymentInvalidStateException refundNotAllowed(Long paymentId, PaymentStatus current) {
        return new PaymentInvalidStateException(
                "Refund not allowed for payment '%d' in status '%s'. Only PAID payments can be refunded."
                        .formatted(paymentId, current));
    }
}
