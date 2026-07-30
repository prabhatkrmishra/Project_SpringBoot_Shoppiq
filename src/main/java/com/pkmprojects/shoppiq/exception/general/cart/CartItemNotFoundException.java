package com.pkmprojects.shoppiq.exception.general.cart;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a cart item cannot be found by its identifier.
 *
 * <p>This exception is thrown by cart service methods when a database
 * lookup for a cart item fails. It uses the
 * {@link ErrorCode#CART_ITEM_NOT_FOUND} code and HTTP 404 Not Found
 * status. The cart item may have been removed by the user, expired due
 * to a timeout, or the ID may be incorrect.</p>
 *
 * <p>The detail message includes the cart item identifier (e.g.,
 * "Cart item with id '42' was not found.") to help the client understand
 * which item was invalid. The client should refresh the cart view and
 * retry the operation.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#CART_ITEM_NOT_FOUND
 * @since 1.0.0
 */
public final class CartItemNotFoundException extends ResourceNotFoundException {

    private CartItemNotFoundException(String detail) {
        super(ErrorCode.CART_ITEM_NOT_FOUND, detail);
    }

    /**
     * Creates an exception for a cart item not found by its identifier.
     *
     * @param id the cart item ID that was not found
     * @return a new exception instance
     */
    public static CartItemNotFoundException id(Long id) {
        return new CartItemNotFoundException(
                "Cart item with id '%d' was not found.".formatted(id)
        );
    }
}
