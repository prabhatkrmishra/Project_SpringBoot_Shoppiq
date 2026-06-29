package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.request.AddCartItemRequest;
import com.pkmprojects.shoppiq.dto.response.CartItemResponse;
import com.pkmprojects.shoppiq.dto.response.CartResponse;
import com.pkmprojects.shoppiq.dto.request.UpdateCartItemRequest;
import com.pkmprojects.shoppiq.entity.User;

/**
 * Contract for shopping cart operations.
 *
 * <p>
 * All methods are scoped to the authenticated user; the user principal
 * must always be supplied by the controller and never inferred from
 * client-provided data.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface CartService {

    /**
     * Adds a product to the user's cart.
     *
     * <p>
     * If the user does not yet have a cart, one is created automatically.
     * If the product is already in the cart, its quantity is increased by
     * the requested amount rather than creating a duplicate line item.
     * </p>
     *
     * @param user    authenticated user
     * @param request add-to-cart payload
     * @return the created or updated cart item
     */
    CartItemResponse create(User user, AddCartItemRequest request);

    /**
     * Returns the full cart for the authenticated user.
     *
     * <p>
     * If the user has no cart an empty cart response is returned rather
     * than throwing an exception, keeping the UI simple.
     * </p>
     *
     * @param user authenticated user
     * @return cart summary including all line items and subtotal
     */
    CartResponse get(User user);

    /**
     * Returns a single cart item belonging to the authenticated user.
     *
     * @param user       authenticated user
     * @param cartItemId ID of the cart item to retrieve
     * @return the cart item response
     */
    CartItemResponse getById(User user, Long cartItemId);

    /**
     * Updates the quantity of an existing cart item.
     *
     * @param user       authenticated user
     * @param cartItemId ID of the cart item to update
     * @param request    new quantity payload
     * @return the updated cart item response
     */
    CartItemResponse update(User user, Long cartItemId, UpdateCartItemRequest request);

    /**
     * Removes a single item from the authenticated user's cart.
     *
     * @param user       authenticated user
     * @param cartItemId ID of the cart item to delete
     */
    void delete(User user, Long cartItemId);
}
