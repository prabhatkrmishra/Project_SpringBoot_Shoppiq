package com.pkmprojects.shoppiq.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response payload for the authenticated user's full cart.
 *
 * @param cartId     ID of the {@code Cart} record
 * @param totalItems total number of line items
 * @param subtotal   sum of all line totals
 * @param items      individual cart item responses
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record CartResponse(
        Long cartId,
        Integer totalItems,
        BigDecimal subtotal,
        List<CartItemResponse> items
) {}
