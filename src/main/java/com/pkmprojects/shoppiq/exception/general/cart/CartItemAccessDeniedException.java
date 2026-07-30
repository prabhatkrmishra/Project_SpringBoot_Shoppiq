package com.pkmprojects.shoppiq.exception.general.cart;

import com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a user attempts to access a cart item that does not belong to their cart.
 *
 * <p>This exception is thrown by cart service methods when a user tries
 * to modify or view a cart item that belongs to another user's cart. It
 * uses the {@link ErrorCode#CART_ITEM_ACCESS_DENIED} code and HTTP 403
 * Forbidden status. Each user has an isolated cart that is not accessible
 * to others.</p>
 *
 * <p>The detail message includes the cart item identifier (e.g.,
 * "Cart item with id '42' does not belong to your cart.") to help the
 * client understand which item was restricted. The client should ensure
 * they are operating on their own cart items.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#CART_ITEM_ACCESS_DENIED
 * @since 1.0.0
 */
public final class CartItemAccessDeniedException extends UnauthorizedOperationException {

    private CartItemAccessDeniedException(String detail) {
        super(ErrorCode.CART_ITEM_ACCESS_DENIED, detail);
    }

    /**
     * Creates an exception for a user attempting to access a cart item they do not own.
     *
     * @param cartItemId the cart item ID
     * @return a new exception instance
     */
    public static CartItemAccessDeniedException forItem(Long cartItemId) {
        return new CartItemAccessDeniedException(
                "Cart item with id '%d' does not belong to your cart.".formatted(cartItemId)
        );
    }
}
