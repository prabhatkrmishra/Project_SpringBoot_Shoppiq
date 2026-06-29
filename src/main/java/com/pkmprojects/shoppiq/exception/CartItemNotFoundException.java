package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a cart item cannot be found.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class CartItemNotFoundException extends ResourceNotFoundException {

    private CartItemNotFoundException(String detail) {
        super(ErrorCode.CART_ITEM_NOT_FOUND, detail);
    }

    public static CartItemNotFoundException id(Long id) {
        return new CartItemNotFoundException(
                "Cart item with id '%d' was not found.".formatted(id)
        );
    }
}
