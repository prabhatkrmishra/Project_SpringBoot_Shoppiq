package com.pkmprojects.shoppiq.dto.order;

import com.pkmprojects.shoppiq.entity.OrderItem;

import java.math.BigDecimal;

/**
 * Response payload representing a single line item inside an order.
 *
 * @author PrabhatKrMishra
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
