package com.pkmprojects.shoppiq.exception.general.payment;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a payment
 * record already exists for the given order.
 *
 * <p>Leaf exception in the duplicate-resource hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.DuplicateResourceException}
 * (HTTP 409) to enforce the one-payment-per-order rule.</p>
 *
 * @author prabhatkrmishra
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
