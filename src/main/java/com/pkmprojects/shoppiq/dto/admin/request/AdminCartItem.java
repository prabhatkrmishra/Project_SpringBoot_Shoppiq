package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Inner item DTO for bulk cart additions by an administrator.
 *
 * <p>This record specifies a single cart line item to be added to a
 * specific user's shopping cart. It is used as an element within
 * {@link BulkCartRequest} for administrative test-data population,
 * enabling administrators to populate multiple user carts in a
 * single API call without requiring each user to perform the action
 * individually.</p>
 *
 * <p>The service layer validates that both {@code userId} and
 * {@code itemDetailsId} reference existing entities before
 * persisting. If the specified item variant is already in the
 * user's cart, the quantity is incremented rather than creating
 * a duplicate line item.</p>
 *
 * @param userId        identifier of the existing user whose cart will
 *                      receive the item; must reference a valid {@code User} entity
 * @param itemDetailsId identifier of the specific product variant
 *                      (the {@code ItemDetails} record) to add;
 *                      different variants of the same product have
 *                      distinct identifiers
 * @param quantity      number of units to add to the cart; must be at least 1
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AdminCartItem(
        /**
         * ID of the existing user whose cart will receive the item.
         */
        @NotNull(message = "User ID is required.")
        Long userId,

        /**
         * ID of the item details (variant) to add.
         */
        @NotNull(message = "Item details ID is required.")
        Long itemDetailsId,

        /**
         * Quantity to add. Must be at least 1.
         */
        @NotNull(message = "Quantity is required.")
        @Min(value = 1, message = "Quantity must be at least 1.")
        Integer quantity
) {
}
