package com.pkmprojects.shoppiq.dto.seller.response;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.enums.DeliveryType;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Seller-facing order response DTO for multi-seller marketplace views.
 *
 * <p>This record shows order details filtered to only the seller's line
 * items. In a multi-seller order, the financial fields (subtotal,
 * grandTotal, etc.) reflect the full order totals from the customer's
 * perspective, while the {@code items} list contains only the products
 * belonging to this specific seller. This separation is essential for
 * marketplace platforms where a single order may contain products from
 * multiple sellers.</p>
 *
 * <p>The static {@link #from(Order, Long)} factory method accepts a
 * {@code sellerId} parameter and filters the order's items to only
 * those owned by that seller. The filtering traverses the
 * {@code OrderItem → ItemDetails → Item → Seller} entity graph to
 * determine ownership.</p>
 *
 * @param id             unique identifier of the order
 * @param status         current order status in the fulfillment lifecycle
 *                       (PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
 * @param paymentMethod  payment method used at checkout (CREDIT_CARD, UPI, COD)
 * @param paymentStatus  current payment status (PENDING, PAID, FAILED, REFUNDED)
 * @param deliveryType   delivery speed selected at checkout (NORMAL, EXPRESS_1DAY)
 * @param subtotal       full order subtotal from all sellers (for context)
 * @param deliveryCharge shipping fee based on the selected delivery type
 * @param codSurcharge   additional surcharge for cash-on-delivery orders
 * @param tax            applicable tax amount computed at checkout time
 * @param discount       promo code discount amount applied to the order
 * @param grandTotal     full order grand total from all sellers (for context)
 * @param placedAt       timestamp when the order was successfully placed
 * @param createdAt      timestamp when the order record was first created
 * @param updatedAt      timestamp of the most recent status or data change
 * @param items          list of seller's line items in this order; filtered to
 *                       contain only products belonging to this seller
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record SellerOrderResponse(
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
         * Full order subtotal (all items from all sellers).
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
         * Discount applied.
         */
        BigDecimal discount,

        /**
         * Full order grand total.
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
         * Seller's line items in this order.
         */
        List<SellerOrderItemResponse> items
) {
    /**
     * Creates a response DTO from the given entity, filtered to the seller's items.
     *
     * @param order    the order entity
     * @param sellerId the seller's identifier for filtering items
     * @return populated response DTO
     */
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
                order.getDeliveryType(),
                order.getSubtotal(),
                order.getDeliveryCharge(),
                order.getCodSurcharge(),
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
