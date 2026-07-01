package com.pkmprojects.shoppiq.dto.order;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Full order detail response.
 *
 * <p>
 * Contains order info, address, payment details, totals and all order items.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record OrderResponse(

        Long id,
        OrderStatus status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        AddressResponse address,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal tax,
        BigDecimal discount,
        BigDecimal grandTotal,
        Instant placedAt,
        Instant createdAt,
        Instant updatedAt,
        List<OrderItemResponse> orderItems
) {

    /**
     * Constructs an {@link OrderResponse} from an {@link Order} entity.
     *
     * @param order source entity
     * @return response DTO
     */
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getOrderItems()
                .stream()
                .map(OrderItemResponse::from)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                AddressResponse.from(order.getAddress()),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getTax(),
                order.getDiscount(),
                order.getGrandTotal(),
                order.getPlacedAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );
    }
}
