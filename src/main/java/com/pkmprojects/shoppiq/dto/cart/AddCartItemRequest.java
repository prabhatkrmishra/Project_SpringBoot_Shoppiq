package com.pkmprojects.shoppiq.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for adding a product variant to the authenticated user's cart.
 *
 * <p>This record is submitted to {@code POST /api/cart} when a customer
 * wants to add an item to their shopping cart. The item is identified
 * by {@code itemDetailsId} rather than the parent item identifier,
 * because different variants (size, color) of the same product have
 * distinct {@code ItemDetails} records with independent pricing and
 * stock levels.</p>
 *
 * <p>If the specified item variant is already present in the user's
 * cart, the service layer increments the existing quantity rather
 * than creating a duplicate line item. The quantity must be at least
 * 1, and the service layer validates that sufficient stock is
 * available before confirming the addition.</p>
 *
 * @param itemDetailsId identifier of the specific product variant
 *                      ({@code ItemDetails} record) to add; must
 *                      reference an existing and active item variant
 * @param quantity      number of units to add to the cart; must be at
 *                      least 1; the service layer checks available stock
 *                      before confirming
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AddCartItemRequest(

        /**
         * ID of the ItemDetails record to add. Must not be null.
         */
        @NotNull(message = "Item details ID is required.")
        Long itemDetailsId,

        /**
         * Number of units. Must be at least 1.
         */
        @NotNull(message = "Quantity is required.")
        @Min(value = 1, message = "Quantity must be at least 1.")
        Integer quantity
) {
}
