package com.pkmprojects.shoppiq.exception.general.payment;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a requested payment record cannot be found.
 *
 * <p>This exception is thrown by payment service methods when a database
 * lookup for a payment fails. It uses the
 * {@link ErrorCode#PAYMENT_NOT_FOUND} code and HTTP 404 Not Found status.
 * The payment ID may be incorrect or the payment may not have been
 * initiated yet.</p>
 *
 * <p>The detail message includes the lookup identifier and type (e.g.,
 * "Payment with id '42' was not found.") to help clients understand which
 * identifier was invalid. The client should verify the payment ID and
 * retry the request.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#PAYMENT_NOT_FOUND
 * @since 1.0.0
 */
public final class PaymentNotFoundException extends ResourceNotFoundException {

    private PaymentNotFoundException(String detail) {
        super(ErrorCode.PAYMENT_NOT_FOUND, detail);
    }

    /**
     * Creates an exception for a payment not found by its identifier.
     *
     * @param id the payment ID
     * @return a new exception instance
     */
    public static PaymentNotFoundException forId(Long id) {
        return new PaymentNotFoundException(
                "Payment with id '%d' was not found.".formatted(id));
    }

    /**
     * Creates an exception for a payment not found by transaction ID.
     *
     * @param transactionId the transaction ID
     * @return a new exception instance
     */
    public static PaymentNotFoundException forTransactionId(String transactionId) {
        return new PaymentNotFoundException(
                "No payment found for transaction id '%s'.".formatted(transactionId));
    }

    /**
     * Creates an exception for a payment not found by order ID.
     *
     * @param orderId the order ID
     * @return a new exception instance
     */
    public static PaymentNotFoundException forOrder(Long orderId) {
        return new PaymentNotFoundException(
                "No payment found for order id '%d'.".formatted(orderId));
    }
}
