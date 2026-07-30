package com.pkmprojects.shoppiq.dto.cart;

import java.math.BigDecimal;

/**
 * Response payload for a single line item in the user's shopping cart.
 *
 * <p>This record represents one product variant in the cart with all the
 * information needed to render the cart line item UI. It includes
 * product identification, pricing details (current and original),
 * quantity, and computed line total. The pricing fields enable the
 * frontend to display discount information and savings prominently.</p>
 *
 * <p>The {@code unitPrice} reflects the current effective price after
 * any item-level discount, while {@code originalPrice} shows the base
 * price before discount. The {@code lineTotal} is computed server-side
 * as {@code unitPrice * quantity} to ensure pricing consistency. The
 * {@code imageUrl} enables product thumbnail rendering in the cart
 * view without requiring a separate product lookup.</p>
 *
 * @param cartItemId    unique identifier of the cart item record
 * @param itemDetailsId identifier of the associated product variant
 *                      ({@code ItemDetails} record)
 * @param itemId        identifier of the parent product item for navigation
 * @param itemSlug      URL-friendly slug for the product, used for
 *                      constructing product detail page links
 * @param itemName      display name of the product
 * @param brand         product manufacturer or brand name
 * @param sku           Stock Keeping Unit identifier for the variant
 * @param unitPrice     current effective unit price after any discount
 * @param originalPrice base unit price before discount; used for
 *                      strikethrough pricing display
 * @param discountPct   discount percentage applied to this product
 * @param quantity      number of units of this variant in the cart
 * @param lineTotal     computed line total as {@code unitPrice * quantity}
 * @param imageUrl      URL of the product's primary image for thumbnail display
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record CartItemResponse(
        /**
         * Unique identifier of the cart item.
         */
        Long cartItemId,

        /**
         * ID of the associated ItemDetails record.
         */
        Long itemDetailsId,

        /**
         * Parent item identifier.
         */
        Long itemId,

        /**
         * URL-friendly slug for the product.
         */
        String itemSlug,

        /**
         * Name of the product.
         */
        String itemName,

        /**
         * Brand of the product.
         */
        String brand,

        /**
         * Stock-keeping unit.
         */
        String sku,

        /**
         * Current unit price (after discount).
         */
        BigDecimal unitPrice,

        /**
         * Original price before discount.
         */
        BigDecimal originalPrice,

        /**
         * Discount percentage applied.
         */
        BigDecimal discountPct,

        /**
         * Number of units in the cart.
         */
        Integer quantity,

        /**
         * Line total (unitPrice × quantity).
         */
        BigDecimal lineTotal,

        /**
         * Product image URL.
         */
        String imageUrl
) {
}
