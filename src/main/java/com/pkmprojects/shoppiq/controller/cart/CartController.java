package com.pkmprojects.shoppiq.controller.cart;

import com.pkmprojects.shoppiq.dto.cart.AddCartItemRequest;
import com.pkmprojects.shoppiq.dto.cart.CartItemResponse;
import com.pkmprojects.shoppiq.dto.cart.CartResponse;
import com.pkmprojects.shoppiq.dto.cart.UpdateCartItemRequest;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.service.cart.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authenticated customer shopping cart operations.
 *
 * <p>Provides endpoints for adding, updating, and removing items from the
 * user's shopping cart. Each operation is scoped to the authenticated user
 * and enforces ownership at the service layer. The cart supports quantity
 * merging (adding an existing item increases its quantity rather than
 * creating a duplicate) and automatic cart creation on first add.</p>
 *
 * <p>This controller acts as the HTTP boundary for cart operations. It
 * delegates all business logic — stock validation, quantity limits,
 * ownership checks, and cart lifecycle management — to {@link CartService}.
 * No business logic resides in the controller.</p>
 *
 * <p>All endpoints are scoped to /user/cart and require CUSTOMER or ADMIN
 * role. The authenticated user is resolved from AuthenticationPrincipal and is
 * never accepted from client-supplied data.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * POST   /user/cart/create         — add an item to the cart
 * GET    /user/cart/get             — retrieve the full cart
 * PUT    /user/cart/update/{id}     — update item quantity
 * DELETE /user/cart/delete/{id}     — remove one item from cart
 * </pre>
 *
 * @author prabhatkrmishra
 * @see CartService
 * @since 1.0.0
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/cart")
public class CartController {

    private final CartService cartService;

    /**
     * Adds a product to the authenticated user's cart.
     *
     * <p>If the user has no cart, one is created automatically. Adding a
     * product that already exists in the cart increases its quantity rather
     * than creating a duplicate line item.</p>
     *
     * @param user    the authenticated user resolved from the JWT
     * @param request the add-to-cart payload containing item details ID and quantity
     * @return 201 Created with the created or updated cart item response
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemResponse create(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return cartService.create(user, request);
    }

    /**
     * Returns the full cart for the authenticated user.
     *
     * <p>Returns an empty cart structure (no items, subtotal zero) when
     * the user has not added any products yet.</p>
     *
     * @param user the authenticated user resolved from the JWT
     * @return 200 OK with cart summary containing all line items and subtotal
     */
    @GetMapping("/get")
    @ResponseStatus(HttpStatus.OK)
    public CartResponse get(@AuthenticationPrincipal(expression = "user") User user) {
        return cartService.get(user);
    }

    /**
     * Updates the quantity of an existing cart item.
     *
     * <p>The new quantity replaces the current one (it does not add to it).
     * Stock availability is validated against the replacement value.</p>
     *
     * @param user       the authenticated user resolved from the JWT
     * @param cartItemId the ID of the cart item to update
     * @param request    the new quantity payload (validated via @Valid)
     * @return 200 OK with the updated cart item response
     */
    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CartItemResponse update(
            @AuthenticationPrincipal(expression = "user") User user,
            @Positive @PathVariable("id") Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.update(user, cartItemId, request);
    }

    /**
     * Removes a single item from the authenticated user's cart.
     *
     * @param user       the authenticated user resolved from the JWT
     * @param cartItemId the ID of the cart item to remove
     */
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal(expression = "user") User user,
            @Positive @PathVariable("id") Long cartItemId
    ) {
        cartService.delete(user, cartItemId);
    }
}
