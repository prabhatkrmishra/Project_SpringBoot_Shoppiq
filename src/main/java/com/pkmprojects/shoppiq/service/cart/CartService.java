package com.pkmprojects.shoppiq.service.cart;

import com.pkmprojects.shoppiq.dto.cart.AddCartItemRequest;
import com.pkmprojects.shoppiq.dto.cart.CartItemResponse;
import com.pkmprojects.shoppiq.dto.cart.CartResponse;
import com.pkmprojects.shoppiq.dto.cart.UpdateCartItemRequest;
import com.pkmprojects.shoppiq.entity.user.User;

/**
 * <strong>Spring Boot Concept:</strong> Contract for shopping cart operations.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * Service layer interface for cart management.
 * Architecture: {@code CartController → CartService → CartRepository / CartItemRepository / ItemDetailsLookupService}.
 * </p>
 *
 * <h2>Business Logic Responsibilities</h2>
 * <ul>
 *   <li>Add items to the user's cart (auto-creates cart if none exists).</li>
 *   <li>Merge quantities when the same product is added twice.</li>
 *   <li>Retrieve cart with calculated subtotals and effective prices.</li>
 *   <li>Update quantities with stock validation.</li>
 *   <li>Remove individual items or clear the entire cart after checkout.</li>
 *   <li>Enforce ownership — users can only access their own cart items.</li>
 * </ul>
 *
 * <p>
 * All methods are scoped to the authenticated user; the user principal
 * must always be supplied by the controller and never inferred from
 * client-provided data.
 * </p>
 *
 * @author prabhatkrmishra
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

    /**
     * Removes all items from the authenticated user's cart.
     *
     * <p>Used after a successful checkout to clear the cart. Operates
     * via {@code orphanRemoval = true} on the {@code Cart.items}
     * collection, so cleared items are deleted from the database.</p>
     *
     * <p>If the user has no cart, this is a no-op — no exception is
     * thrown.</p>
     *
     * @param user authenticated user whose cart should be cleared
     */
    void clearCart(User user);
}
