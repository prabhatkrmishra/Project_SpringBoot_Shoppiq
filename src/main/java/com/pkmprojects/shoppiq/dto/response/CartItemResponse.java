package com.pkmprojects.shoppiq.dto.response;

import java.math.BigDecimal;

/**
 * Response payload for a single cart line item.
 *
 * @param cartItemId    ID of the {@code CartItem} record
 * @param itemDetailsId ID of the associated {@code ItemDetails}
 * @param itemName      name of the product
 * @param brand         brand of the product
 * @param sku           stock-keeping unit
 * @param unitPrice     current unit price (after discount)
 * @param originalPrice original price before discount
 * @param discountPct   discount percentage applied
 * @param quantity      units in the cart
 * @param lineTotal     unitPrice × quantity
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record CartItemResponse(
        Long cartItemId,
        Long itemDetailsId,
        String itemName,
        String brand,
        String sku,
        BigDecimal unitPrice,
        BigDecimal originalPrice,
        BigDecimal discountPct,
        Integer quantity,
        BigDecimal lineTotal
) {}
