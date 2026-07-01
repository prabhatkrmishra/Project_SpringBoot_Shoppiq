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
        String itemNameSnapshot,
        BigDecimal unitPriceSnapshot,
        Integer quantity,
        BigDecimal subtotal
) {

    /**
     * Constructs an {@link OrderItemResponse} from an {@link OrderItem} entity.
     *
     * @param orderItem source entity
     * @return response DTO
     */
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getItemDetails() != null ? orderItem.getItemDetails().getId() : null,
                orderItem.getItemNameSnapshot(),
                orderItem.getUnitPriceSnapshot(),
                orderItem.getQuantity(),
                orderItem.getSubtotal()
        );
    }
}
