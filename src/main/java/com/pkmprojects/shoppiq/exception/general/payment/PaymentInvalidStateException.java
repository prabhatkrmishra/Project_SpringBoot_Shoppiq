package com.pkmprojects.shoppiq.exception.general.payment;

import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when an operation
 * cannot be performed because the payment is in an incompatible state.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) to enforce the payment lifecycle state machine. Each factory
 * method documents a specific illegal transition.</p>
 *
 * @author prabhatkrmishra
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
