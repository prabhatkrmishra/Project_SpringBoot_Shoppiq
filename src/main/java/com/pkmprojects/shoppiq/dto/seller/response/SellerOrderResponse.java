package com.pkmprojects.shoppiq.dto.seller.response;

import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.OrderItem;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Seller-facing order response DTO.
 *
 * <p>Shows order details filtered to only the seller's line items.
 * In a multi-seller order, the financial fields (subtotal, grandTotal)
 * reflect the full order totals; the seller's own items are listed
 * in {@code items}.</p>
 *
 * @param id            order identifier
 * @param status        order status
 * @param paymentMethod payment method used
 * @param paymentStatus payment status
 * @param subtotal      full order subtotal
 * @param shippingFee   shipping fee
 * @param tax           tax amount
 * @param discount      discount applied
 * @param grandTotal    full order grand total
 * @param placedAt      when the order was placed
 * @param createdAt     entity creation timestamp
 * @param updatedAt     entity last update timestamp
 * @param items         seller's line items in this order
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record SellerOrderResponse(
        Long id,
        OrderStatus status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal tax,
        BigDecimal discount,
        BigDecimal grandTotal,
        Instant placedAt,
        Instant createdAt,
        Instant updatedAt,
        List<SellerOrderItemResponse> items
) {
    public static SellerOrderResponse from(Order order, Long sellerId) {
        List<SellerOrderItemResponse> filteredItems = order.getOrderItems()
                .stream()
                .filter(oi -> oi.getItemDetails() != null
                        && oi.getItemDetails().getItem() != null
                        && oi.getItemDetails().getItem().getSeller() != null
                        && oi.getItemDetails().getItem().getSeller().getId().equals(sellerId))
                .map(SellerOrderItemResponse::from)
                .toList();

        return new SellerOrderResponse(
                order.getId(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getTax(),
                order.getDiscount(),
                order.getGrandTotal(),
                order.getPlacedAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                filteredItems
        );
    }
}
