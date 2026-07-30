package com.pkmprojects.shoppiq.exception.general.payment;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a payment record already exists for the given order.
 *
 * <p>This exception is thrown when attempting to create a duplicate
 * payment for an order that already has an associated payment. Each order
 * can have at most one payment record. It uses the
 * {@link ErrorCode#PAYMENT_ALREADY_EXISTS} code and HTTP 409 Conflict
 * status.</p>
 *
 * <p>The detail message includes the order identifier (e.g.,
 * "A payment already exists for order id '42'.") to help the client
 * understand which order had the conflict. The client should check the
 * existing payment status instead of creating a new one.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#PAYMENT_ALREADY_EXISTS
 * @since 1.0.0
 */
public final class DuplicatePaymentException extends DuplicateResourceException {

    private DuplicatePaymentException(String detail) {
        super(ErrorCode.PAYMENT_ALREADY_EXISTS, detail);
    }

    /**
     * Creates an exception for a duplicate payment on an order.
     *
     * @param orderId the order ID
     * @return a new exception instance
     */
    public static DuplicatePaymentException forOrder(Long orderId) {
        return new DuplicatePaymentException(
                "A payment already exists for order id '%d'.".formatted(orderId));
    }
}
