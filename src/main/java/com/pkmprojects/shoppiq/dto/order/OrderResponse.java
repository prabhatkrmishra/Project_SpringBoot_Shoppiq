package com.pkmprojects.shoppiq.dto.order;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.order.OrderAddressSnapshot;
import com.pkmprojects.shoppiq.entity.address.Address;
import com.pkmprojects.shoppiq.enums.DeliveryType;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Full order detail response.
 *
 * <p>
 * Contains order info, address, payment details, totals and all order items.
 * </p>
 *
 * <p><b>Address snapshot pattern:</b> The private {@code toAddressResponse()}
 * method first tries the frozen {@code OrderAddressSnapshot} (captured at
 * checkout), falling back to the user's live {@code Address} entity for
 * legacy orders. This ensures historical order accuracy.</p>
 *
 * <p><b>Composition:</b> This DTO composes {@link AddressResponse} and
 * {@link OrderItemResponse}, demonstrating how complex responses are built
 * from smaller, reusable DTOs.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record OrderResponse(

        Long id,
        OrderStatus status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        DeliveryType deliveryType,
        AddressResponse address,
        BigDecimal subtotal,
        BigDecimal deliveryCharge,
        BigDecimal codSurcharge,
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

        AddressResponse address = toAddressResponse(order.getShippingAddress(), order.getAddress());

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getDeliveryType(),
                address,
                order.getSubtotal(),
                order.getDeliveryCharge(),
                order.getCodSurcharge(),
                order.getTax(),
                order.getDiscount(),
                order.getGrandTotal(),
                order.getPlacedAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );
    }

    /**
     * Builds an {@link AddressResponse} from the snapshot, falling back to
     * the live address entity for legacy orders that predate the snapshot.
     */
    private static AddressResponse toAddressResponse(OrderAddressSnapshot snapshot,
                                                     Address liveAddress) {
        if (snapshot != null) {
            return new AddressResponse(
                    null, null,
                    snapshot.getFullName(), snapshot.getPhone(),
                    snapshot.getLine1(), snapshot.getLine2(),
                    snapshot.getCity(), snapshot.getState(),
                    snapshot.getPostalCode(), snapshot.getCountry(),
                    false, null, null
            );
        }
        if (liveAddress != null) {
            return AddressResponse.from(liveAddress);
        }
        return null;
    }
}
