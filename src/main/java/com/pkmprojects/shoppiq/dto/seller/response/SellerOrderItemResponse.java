package com.pkmprojects.shoppiq.dto.seller.response;

import com.pkmprojects.shoppiq.entity.order.OrderItem;

import java.math.BigDecimal;

/**
 * Seller-facing order item response DTO for multi-seller order views.
 *
 * <p>This record shows only the line items that belong to the seller's
 * products within a multi-seller order. In a marketplace platform where
 * a single order may contain products from multiple sellers, this DTO
 * ensures each seller sees only their own items while the parent
 * {@link SellerOrderResponse} provides the full order context.</p>
 *
 * <p>The product name and unit price are snapshots captured at the time
 * of purchase, preserving historical accuracy even if the product's
 * name or price changes after the order. The static
 * {@link #from(OrderItem)} factory method extracts only the fields
 * needed for the seller's view.</p>
 *
 * @param id                unique identifier of the order item record
 * @param itemNameSnapshot  product name at the time of purchase;
 *                          preserved as an immutable snapshot
 * @param unitPriceSnapshot unit price at the time of purchase;
 *                          preserved as an immutable snapshot
 * @param quantity          number of units ordered for this line item
 * @param subtotal          line total computed as {@code unitPriceSnapshot * quantity}
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record SellerOrderItemResponse(
        /**
         * Unique identifier of the order item.
         */
        Long id,

        /**
         * Product name at time of purchase (snapshot).
         */
        String itemNameSnapshot,

        /**
         * Unit price at time of purchase (snapshot).
         */
        BigDecimal unitPriceSnapshot,

        /**
         * Quantity ordered.
         */
        int quantity,

        /**
         * Line total (unit price × quantity).
         */
        BigDecimal subtotal
) {
    /**
     * Creates a response DTO from the given entity.
     *
     * @param orderItem the order item entity
     * @return populated response DTO
     */
    public static SellerOrderItemResponse from(OrderItem orderItem) {
        return new SellerOrderItemResponse(
                orderItem.getId(),
                orderItem.getItemNameSnapshot(),
                orderItem.getUnitPriceSnapshot(),
                orderItem.getQuantity(),
                orderItem.getSubtotal()
        );
    }
}
