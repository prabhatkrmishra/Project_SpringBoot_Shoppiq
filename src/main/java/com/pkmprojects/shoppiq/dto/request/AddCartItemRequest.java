package com.pkmprojects.shoppiq.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for adding a product to the cart.
 *
 * @param itemDetailsId ID of the {@code ItemDetails} record to add
 * @param quantity      number of units (minimum 1)
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record AddCartItemRequest(

        @NotNull(message = "Item details ID is required.")
        Long itemDetailsId,

        @NotNull(message = "Quantity is required.")
        @Min(value = 1, message = "Quantity must be at least 1.")
        Integer quantity
) {}
