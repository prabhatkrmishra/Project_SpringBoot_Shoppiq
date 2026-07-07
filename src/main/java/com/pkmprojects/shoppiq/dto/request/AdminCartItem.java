package com.pkmprojects.shoppiq.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Inner item DTO used by {@link BulkCartRequest} for bulk cart additions.
 *
 * <p>
 * Each item specifies a target user, a product variant (item details) ID,
 * and the quantity to add.
 * </p>
 *
 * @param userId        ID of the existing user whose cart will receive the item
 * @param itemDetailsId ID of the item details (variant) to add
 * @param quantity      quantity to add (must be at least 1)
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record AdminCartItem(
        @NotNull(message = "User ID is required.")
        Long userId,

        @NotNull(message = "Item details ID is required.")
        Long itemDetailsId,

        @NotNull(message = "Quantity is required.")
        @Min(value = 1, message = "Quantity must be at least 1.")
        Integer quantity
) {
}
