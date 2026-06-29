package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user attempts to access a cart item
 * that does not belong to their cart.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class CartItemAccessDeniedException extends ShoppiqException {

    private CartItemAccessDeniedException(String detail) {
        super(ErrorCode.CART_ITEM_ACCESS_DENIED, HttpStatus.FORBIDDEN, detail);
    }

    public static CartItemAccessDeniedException forItem(Long cartItemId) {
        return new CartItemAccessDeniedException(
                "Cart item with id '%d' does not belong to your cart.".formatted(cartItemId)
        );
    }
}
