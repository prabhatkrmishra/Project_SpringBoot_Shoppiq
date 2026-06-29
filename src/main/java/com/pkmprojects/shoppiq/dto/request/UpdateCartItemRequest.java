package com.pkmprojects.shoppiq.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for updating the quantity of an existing cart item.
 *
 * @param quantity the new quantity (minimum 1)
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record UpdateCartItemRequest(

        @NotNull(message = "Quantity is required.")
        @Min(value = 1, message = "Quantity must be at least 1.")
        Integer quantity
) {}
