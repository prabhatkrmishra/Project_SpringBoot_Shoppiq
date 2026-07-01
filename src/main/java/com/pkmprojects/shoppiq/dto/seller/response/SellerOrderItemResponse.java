package com.pkmprojects.shoppiq.dto.seller.response;

import com.pkmprojects.shoppiq.entity.OrderItem;

import java.math.BigDecimal;

/**
 * Seller-facing order item response DTO.
 *
 * <p>Shows only the line items that belong to the seller's products
 * within a multi-seller order.</p>
 *
 * @param id                order item identifier
 * @param itemNameSnapshot  product name at time of purchase
 * @param unitPriceSnapshot unit price at time of purchase
 * @param quantity          quantity ordered
 * @param subtotal          line total
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record SellerOrderItemResponse(
        Long id,
        String itemNameSnapshot,
        BigDecimal unitPriceSnapshot,
        int quantity,
        BigDecimal subtotal
) {
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
