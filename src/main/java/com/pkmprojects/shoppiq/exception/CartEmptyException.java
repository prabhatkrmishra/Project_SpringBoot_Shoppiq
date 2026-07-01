package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a checkout is attempted on an empty cart.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class CartEmptyException extends InvalidOperationException {

    public CartEmptyException() {
        super(ErrorCode.CART_EMPTY, "Cannot checkout with an empty cart.");
    }
}
