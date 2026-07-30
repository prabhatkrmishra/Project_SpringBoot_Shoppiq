package com.pkmprojects.shoppiq.dto.order;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.entity.address.Address;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.order.OrderAddressSnapshot;
import com.pkmprojects.shoppiq.enums.DeliveryType;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Full order detail response for customer-facing order history.
 *
 * <p>This record provides a comprehensive view of an order including
 * status, payment information, financial breakdown, shipping address,
 * and all order line items. It is returned by the order detail endpoint
 * and is designed for the customer's order history and order detail
 * pages where full order information is needed.</p>
 *
 * <p>The address field uses the snapshot pattern: the shipping address
 * is captured at checkout time and stored as an immutable snapshot.
 * The private {@code toAddressResponse()} method first tries the
 * frozen snapshot, falling back to the user's live address entity for
 * legacy orders that predate the snapshot feature. This ensures
 * historical order accuracy. The DTO composes {@link AddressResponse}
 * and {@link OrderItemResponse} as nested records.</p>
 *
 * @param id             unique identifier of the order
 * @param status         current order status in the fulfillment lifecycle
 *                       (PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
 * @param paymentMethod  payment method used at checkout (CREDIT_CARD, UPI, COD)
 * @param paymentStatus  current payment status (PENDING, PAID, FAILED, REFUNDED)
 * @param deliveryType   delivery speed selected at checkout (NORMAL, EXPRESS_1DAY)
 * @param address        shipping address snapshot (or live fallback for legacy orders)
 * @param subtotal       sum of all line item totals before fees or discounts
 * @param deliveryCharge shipping fee based on the selected delivery type
 * @param codSurcharge   additional surcharge for cash-on-delivery orders
 * @param tax            applicable tax amount computed at checkout time
 * @param discount       promo code discount amount applied to the order
 * @param grandTotal     final amount the customer pays, inclusive of all fees
 * @param placedAt       timestamp when the order was successfully placed
 * @param createdAt      timestamp when the order record was first created
 * @param updatedAt      timestamp of the most recent status or data change
 * @param orderItems     list of order line items with product snapshots and quantities
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record OrderResponse(

        /**
         * Unique identifier of the order.
         */
        Long id,

        /**
         * Current order status.
         */
        OrderStatus status,

        /**
         * Payment method used for this order.
         */
        PaymentMethod paymentMethod,

        /**
         * Current payment status.
         */
        PaymentStatus paymentStatus,

        /**
         * Delivery type (NORMAL, EXPRESS_1DAY).
         */
        DeliveryType deliveryType,

        /**
         * Shipping address. May be a snapshot or live address.
         */
        AddressResponse address,

        /**
         * Sum of all item line totals before fees or discounts.
         */
        BigDecimal subtotal,

        /**
         * Delivery charge based on delivery type.
         */
        BigDecimal deliveryCharge,

        /**
         * Cash-on-delivery surcharge.
         */
        BigDecimal codSurcharge,

        /**
         * Tax amount.
         */
        BigDecimal tax,

        /**
         * Promo code discount amount.
         */
        BigDecimal discount,

        /**
         * Final total the customer pays.
         */
        BigDecimal grandTotal,

        /**
         * When the order was placed.
         */
        Instant placedAt,

        /**
         * Entity creation timestamp.
         */
        Instant createdAt,

        /**
         * Entity last update timestamp.
         */
        Instant updatedAt,

        /**
         * Order line items.
         */
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
