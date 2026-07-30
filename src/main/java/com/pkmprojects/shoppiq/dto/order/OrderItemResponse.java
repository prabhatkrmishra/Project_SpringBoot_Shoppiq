package com.pkmprojects.shoppiq.dto.order;

import com.pkmprojects.shoppiq.entity.order.OrderItem;

import java.math.BigDecimal;

/**
 * Response payload representing a single line item inside an order.
 *
 * <p>This record uses the price snapshotting pattern: the product name
 * and unit price are captured at the time of purchase and stored as
 * immutable snapshots on the order item. This ensures that order
 * history remains accurate even if the product's name, price, or
 * availability changes after the purchase. This is a critical
 * e-commerce pattern for financial and legal compliance.</p>
 *
 * <p>The static {@link #from(OrderItem)} factory method handles
 * nullable entity associations gracefully, returning null for
 * image URL, item identifier, and slug when the original product
 * has been deleted from the catalog. This ensures order history
 * remains intact even after product removal.</p>
 *
 * @param id                unique identifier of the order item record
 * @param itemDetailsId     identifier of the product variant at time of purchase
 * @param itemId            identifier of the parent product item; nullable if the
 *                          product has been deleted from the catalog
 * @param itemSlug          URL-friendly slug for the product; nullable if the
 *                          product has been deleted
 * @param itemNameSnapshot  product name at the time of purchase;
 *                          preserved as an immutable snapshot
 * @param unitPriceSnapshot unit price at the time of purchase;
 *                          preserved as an immutable snapshot
 * @param quantity          number of units ordered for this line item
 * @param subtotal          line total computed as {@code unitPriceSnapshot * quantity}
 * @param imageUrl          product image URL at time of purchase; nullable if the
 *                          product has been deleted
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record OrderItemResponse(

        /**
         * Unique identifier of the order item.
         */
        Long id,

        /**
         * Item details (variant) identifier.
         */
        Long itemDetailsId,

        /**
         * Parent item identifier. Nullable if item was deleted.
         */
        Long itemId,

        /**
         * URL-friendly slug for the product. Nullable if item was deleted.
         */
        String itemSlug,

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
        Integer quantity,

        /**
         * Line total (unit price × quantity).
         */
        BigDecimal subtotal,

        /**
         * Product image URL. Nullable if item was deleted.
         */
        String imageUrl
) {

    /**
     * Constructs an {@link OrderItemResponse} from an {@link OrderItem} entity.
     *
     * @param orderItem source entity
     * @return response DTO
     */
    public static OrderItemResponse from(OrderItem orderItem) {
        String imageUrl = null;
        Long itemId = null;
        String itemSlug = null;
        if (orderItem.getItemDetails() != null) {
            imageUrl = orderItem.getItemDetails().getImageUrl();
            if (orderItem.getItemDetails().getItem() != null) {
                itemId = orderItem.getItemDetails().getItem().getId();
                itemSlug = orderItem.getItemDetails().getItem().getSlug();
            }
        }
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getItemDetails() != null ? orderItem.getItemDetails().getId() : null,
                itemId,
                itemSlug,
                orderItem.getItemNameSnapshot(),
                orderItem.getUnitPriceSnapshot(),
                orderItem.getQuantity(),
                orderItem.getSubtotal(),
                imageUrl
        );
    }
}
