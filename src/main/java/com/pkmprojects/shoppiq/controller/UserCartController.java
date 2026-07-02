package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.dto.request.AddCartItemRequest;
import com.pkmprojects.shoppiq.dto.response.CartItemResponse;
import com.pkmprojects.shoppiq.dto.response.CartResponse;
import com.pkmprojects.shoppiq.dto.request.UpdateCartItemRequest;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that exposes shopping-cart endpoints for authenticated
 * customers.
 *
 * <p>
 * All endpoints are scoped to {@code /user/cart} and require the
 * {@code CUSTOMER} (or {@code ADMIN}) role. The authenticated user is
 * resolved from {@link AuthenticationPrincipal} and is never accepted
 * from client-supplied data.
 * </p>
 *
 * <h2>Endpoints</h2>
 * <ul>
 *     <li>{@code POST   /user/cart/create}     — add an item to the cart</li>
 *     <li>{@code GET    /user/cart/get}         — get the full cart</li>
 *     <li>{@code PUT    /user/cart/update/{id}} — update item quantity</li>
 *     <li>{@code DELETE /user/cart/delete/{id}} — remove one item</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/cart")
public class UserCartController {

    private final CartService cartService;

    /**
     * Adds a product to the authenticated user's cart.
     *
     * <p>
     * If the user has no cart one is created automatically. Adding a product
     * that already exists in the cart increases its quantity.
     * </p>
     *
     * @param user    the authenticated user (from JWT)
     * @param request payload containing the item details ID and quantity
     * @return the created or updated cart item, HTTP 201
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemResponse create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return cartService.create(user, request);
    }

    /**
     * Returns the full cart for the authenticated user.
     *
     * <p>
     * Returns an empty cart structure (no items, subtotal zero) when the
     * user has not added any products yet.
     * </p>
     *
     * @param user the authenticated user
     * @return cart summary with all line items and subtotal
     */
    @GetMapping("/get")
    @ResponseStatus(HttpStatus.OK)
    public CartResponse get(@AuthenticationPrincipal User user) {
        return cartService.get(user);
    }

    /**
     * Updates the quantity of an existing cart item.
     *
     * <p>
     * The new quantity replaces the current one (it does not add to it).
     * Stock is validated against the replacement value.
     * </p>
     *
     * @param user       the authenticated user
     * @param cartItemId ID of the cart item to update
     * @param request    new quantity payload
     * @return the updated cart item
     */
    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CartItemResponse update(
            @AuthenticationPrincipal User user,
            @Positive @PathVariable("id") Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.update(user, cartItemId, request);
    }

    /**
     * Removes a single item from the authenticated user's cart.
     *
     * @param user       the authenticated user
     * @param cartItemId ID of the cart item to remove
     */
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal User user,
            @Positive @PathVariable("id") Long cartItemId
    ) {
        cartService.delete(user, cartItemId);
    }
}
