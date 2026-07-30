package com.pkmprojects.shoppiq.dto.cart;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response payload for the authenticated user's full shopping cart.
 *
 * <p>This record wraps a list of {@link CartItemResponse} entries
 * together with summary fields that provide aggregate cart metrics.
 * It is returned by {@code GET /api/cart} and is the primary data
 * structure the frontend uses to render the cart page. The
 * {@code subtotal} is computed server-side from all line totals,
 * ensuring the frontend always displays authoritative pricing
 * regardless of client-side calculation errors.</p>
 *
 * <p>The {@code totalItems} count represents the total number of
 * distinct line items (not the total quantity of all products). The
 * {@code subtotal} is the sum of all {@code lineTotal} values before
 * any delivery charges, taxes, or promo discounts are applied at
 * checkout.</p>
 *
 * @param cartId     unique identifier of the cart entity; each user has
 *                   exactly one active cart
 * @param totalItems total number of distinct line items in the cart;
 *                   determines the cart badge count in the navigation
 * @param subtotal   sum of all line totals before fees, taxes, or discounts;
 *                   computed server-side for pricing accuracy
 * @param items      list of individual cart item responses, each containing
 *                   product details, pricing, and quantity information
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record CartResponse(
        /**
         * Unique identifier of the cart.
         */
        Long cartId,

        /**
         * Total number of line items in the cart.
         */
        Integer totalItems,

        /**
         * Sum of all line totals before fees or discounts.
         */
        BigDecimal subtotal,

        /**
         * Individual cart item responses.
         */
        List<CartItemResponse> items
) {
}
