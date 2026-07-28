package com.pkmprojects.shoppiq.exception.general.cart;

import com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a user attempts
 * to access a cart item that does not belong to their cart.
 *
 * <p>Leaf exception in the authorization hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException}
 * (HTTP 403) for cart item ownership violations.</p>
 *
 * @author prabhatkrmishra
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
