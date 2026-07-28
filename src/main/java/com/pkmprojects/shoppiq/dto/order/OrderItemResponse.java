package com.pkmprojects.shoppiq.dto.order;

import com.pkmprojects.shoppiq.entity.order.OrderItem;

import java.math.BigDecimal;

/**
 * Response payload representing a single line item inside an order.
 *
 * <p>This Java record uses <b>price snapshotting</b> — the {@code itemNameSnapshot}
 * and {@code unitPriceSnapshot} preserve the values at the time of purchase,
 * so order history remains accurate even if the product's name or price changes
 * later. This is a critical e-commerce pattern.</p>
 *
 * <p><b>Null-safe mapping:</b> The {@link #from(com.pkmprojects.shoppiq.entity.order.OrderItem) from()}
 * method handles nullable {@code ItemDetails} references gracefully, extracting
 * image URL and item ID only when available.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record OrderItemResponse(

        Long id,
        Long itemDetailsId,
        Long itemId,
        String itemSlug,
        String itemNameSnapshot,
        BigDecimal unitPriceSnapshot,
        Integer quantity,
        BigDecimal subtotal,
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
