package com.pkmprojects.shoppiq.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * <strong>Spring Boot Concept:</strong> Request body for adding a product to the cart.
 *
 * <p>This Java record uses {@code @NotNull} and {@code @Min} to enforce that
 * the frontend always provides a valid item variant ID and a positive quantity.
 * Spring Boot's validation auto-rejects requests with missing or zero quantities.</p>
 *
 * <p><b>API contract:</b> POST /api/cart — the item variant is identified by
 * {@code itemDetailsId} (not the item itself), since different variants (size,
 * color) of the same product have different {@code ItemDetails} records.</p>
 *
 * @param itemDetailsId ID of the {@code ItemDetails} record to add
 * @param quantity      number of units (minimum 1)
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AddCartItemRequest(

        @NotNull(message = "Item details ID is required.")
        Long itemDetailsId,

        @NotNull(message = "Quantity is required.")
        @Min(value = 1, message = "Quantity must be at least 1.")
        Integer quantity
) {}
