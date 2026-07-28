package com.pkmprojects.shoppiq.exception.general.cart;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a cart item
 * cannot be found.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) for missing {@link com.pkmprojects.shoppiq.entity.cart.CartItem}
 * entities.</p>
 *
 * @author prabhatkrmishra
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
