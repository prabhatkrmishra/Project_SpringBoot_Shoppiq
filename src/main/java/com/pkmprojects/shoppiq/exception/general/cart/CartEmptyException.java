package com.pkmprojects.shoppiq.exception.general.cart;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a checkout is attempted on an empty cart.
 *
 * <p>This exception is thrown during the checkout process when the
 * customer's cart contains no items. It uses the
 * {@link ErrorCode#CART_EMPTY} code and HTTP 400 Bad Request status.
 * The customer must add items to the cart before proceeding to checkout.</p>
 *
 * <p>The detail message is a fixed string ("Cannot checkout with an
 * empty cart.") that clearly explains the issue. The client should
 * redirect the user to the product catalog to add items before
 * retrying the checkout.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#CART_EMPTY
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
