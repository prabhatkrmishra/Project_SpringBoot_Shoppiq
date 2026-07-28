package com.pkmprojects.shoppiq.exception.general.cart;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a checkout is
 * attempted on an empty cart.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) to enforce the business rule that checkout requires a non-empty
 * cart.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class CartEmptyException extends InvalidOperationException {

    /**
     * Creates a new cart empty exception.
     */
    public CartEmptyException() {
        super(ErrorCode.CART_EMPTY, "Cannot checkout with an empty cart.");
    }
}
