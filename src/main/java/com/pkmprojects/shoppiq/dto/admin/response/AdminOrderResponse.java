package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.enums.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO for admin order management.
 *
 * <p>
 * This DTO provides a comprehensive view of an order for administrators,
 * including customer details, shipping address, payment status, and
 * line items. It supports the order management workflow with status
 * transitions.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose complete order details to admin API.</li>
 *     <li>Support order status transition operations.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java Records.</li>
 *     <li>Includes nested records for address and line items.</li>
 *     <li>Created using {@link #fromEntity(Order)}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record AdminOrderResponse(

        /**
         * Order identifier.
         */
        Long id,

        /**
         * Customer username.
         */
        String customerUsername,

        /**
         * Customer email.
         */
        String customerEmail,

        /**
         * Shipping address.
         */
        AddressResponse address,

        /**
         * Current order status.
         */
        OrderStatus status,

        /**
         * Payment method used.
         */
        PaymentMethod paymentMethod,

        /**
         * Current payment status.
         */
        PaymentStatus paymentStatus,

        /**
         * Order subtotal.
         */
        BigDecimal subtotal,

        /**
         * Shipping fee.
         */
        BigDecimal shippingFee,

        /**
         * Tax amount.
         */
        BigDecimal tax,

        /**
         * Discount applied.
         */
        BigDecimal discount,

        /**
         * Grand total payable.
         */
        BigDecimal grandTotal,

        /**
         * Order placement timestamp.
         */
        Instant placedAt,

        /**
         * Order line items.
         */
        List<OrderItemResponse> items
) {

    /**
     * Shipping address data.
     */
    public record AddressResponse(

            /**
             * Address label.
             */
            String label,

            /**
             * Recipient full name.
             */
            String fullName,

            /**
             * Contact phone.
             */
            String phone,

            /**
             * Address line 1.
             */
            String line1,

            /**
             * Address line 2.
             */
            String line2,

            /**
             * City.
             */
            String city,

            /**
             * State/Province.
             */
            String state,

            /**
             * Postal code.
             */
            String postalCode,

            /**
             * Country.
             */
            String country
    ) {
    }

    /**
     * Order line item data.
     */
    public record OrderItemResponse(

            /**
             * Order item identifier.
             */
            Long id,

            /**
             * Product name snapshot at purchase.
             */
            String itemNameSnapshot,

            /**
             * Unit price snapshot at purchase.
             */
            BigDecimal unitPriceSnapshot,

            /**
             * Quantity ordered.
             */
            int quantity,

            /**
             * Line subtotal (unit price × quantity).
             */
            BigDecimal subtotal
    ) {
    }

    /**
     * Creates an {@code AdminOrderResponse} from an {@link Order} entity.
     *
     * @param order order entity
     * @return mapped response DTO
     */
    public static AdminOrderResponse fromEntity(Order order) {
        Address address = order.getAddress();
        AddressResponse addressResponse = new AddressResponse(
                address.getLabel(),
                address.getFullName(),
                address.getPhone(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry()
        );

        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getItemNameSnapshot(),
                        item.getUnitPriceSnapshot(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();

        return new AdminOrderResponse(
                order.getId(),
                order.getUser().getUsername(),
                order.getUser().getEmail(),
                addressResponse,
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getTax(),
                order.getDiscount(),
                order.getGrandTotal(),
                order.getPlacedAt(),
                itemResponses
        );
    }
}