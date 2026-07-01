package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a payment record already exists for the given order.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class DuplicatePaymentException extends DuplicateResourceException {

    public DuplicatePaymentException(Long orderId) {
        super(ErrorCode.PAYMENT_ALREADY_EXISTS,
                "A payment already exists for order id '%d'.".formatted(orderId));
    }
}
