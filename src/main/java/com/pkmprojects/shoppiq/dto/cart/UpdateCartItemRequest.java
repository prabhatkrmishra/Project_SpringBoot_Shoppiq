package com.pkmprojects.shoppiq.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for updating the quantity of an existing cart line item.
 *
 * <p>This record is submitted to {@code PUT /api/cart/{cartItemId}} when
 * a customer wants to change the quantity of a product already in their
 * shopping cart. The cart item is identified by the path parameter
 * {@code cartItemId}, while this DTO carries only the new quantity
 * value.</p>
 *
 * <p>The quantity must be at least 1. If the customer wants to remove an
 * item from the cart entirely, they should use the DELETE endpoint
 * instead. The service layer validates that sufficient stock is
 * available for the requested quantity before confirming the update.</p>
 *
 * @param quantity the new quantity for this cart item; must be at least 1;
 *                 the service layer checks available inventory before
 *                 applying the change
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record UpdateCartItemRequest(

        /**
         * New quantity. Must be at least 1.
         */
        @NotNull(message = "Quantity is required.")
        @Min(value = 1, message = "Quantity must be at least 1.")
        Integer quantity
) {
}
