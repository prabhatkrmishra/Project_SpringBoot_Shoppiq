package com.pkmprojects.shoppiq.dto.order;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.enums.DeliveryType;
import com.pkmprojects.shoppiq.enums.OrderStatus;

import java.math.BigDecimal;

/**
 * Lightweight response returned immediately after a successful checkout.
 *
 * <p>This record contains just enough information for the frontend to
 * redirect the user to the order confirmation or payment page. It
 * includes the {@code paymentId} for immediate payment verification
 * and the financial summary for display on the confirmation screen.
 * Unlike the full {@link OrderResponse}, this DTO omits line items
 * and detailed address information to minimize payload size on the
 * critical checkout path.</p>
 *
 * <p>The static {@link #from(Order, Long)} factory method constructs
 * this response from the newly created order entity and the associated
 * payment identifier. All financial fields are extracted directly from
 * the order, which has already computed them server-side during
 * checkout.</p>
 *
 * @param orderId        unique identifier of the newly created order
 * @param status         initial order status, typically PLACED
 * @param subtotal       sum of all item line totals before fees or discounts
 * @param discount       promo code discount amount applied to the order
 * @param deliveryCharge shipping fee based on the selected delivery type
 * @param codSurcharge   additional surcharge for cash-on-delivery orders
 * @param grandTotal     final amount the customer pays, inclusive of all fees
 * @param deliveryType   delivery speed selected at checkout (NORMAL, EXPRESS_1DAY)
 * @param paymentId      identifier of the payment record created for this
 *                       order; used for immediate payment verification
 * @param promoCode      promo code string used at checkout; null if none
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record CheckoutResponse(

        /**
         * Unique identifier of the newly created order.
         */
        Long orderId,

        /**
         * Initial order status (typically PLACED).
         */
        OrderStatus status,

        /**
         * Sum of all item line totals.
         */
        BigDecimal subtotal,

        /**
         * Promo code discount amount.
         */
        BigDecimal discount,

        /**
         * Delivery charge based on delivery type.
         */
        BigDecimal deliveryCharge,

        /**
         * Cash-on-delivery surcharge.
         */
        BigDecimal codSurcharge,

        /**
         * Final total the customer pays.
         */
        BigDecimal grandTotal,

        /**
         * Delivery type selected at checkout.
         */
        DeliveryType deliveryType,

        /**
         * Payment ID for immediate payment verification.
         */
        Long paymentId,

        /**
         * Promo code used at checkout. Null if none.
         */
        String promoCode
) {

    /**
     * Constructs a {@link CheckoutResponse} from an {@link Order} entity.
     *
     * @param order     the newly created order
     * @param paymentId id of the payment created for this order
     * @return checkout response
     */
    public static CheckoutResponse from(Order order, Long paymentId) {
        return new CheckoutResponse(
                order.getId(),
                order.getStatus(),
                order.getSubtotal(),
                order.getDiscount(),
                order.getDeliveryCharge(),
                order.getCodSurcharge(),
                order.getGrandTotal(),
                order.getDeliveryType(),
                paymentId,
                order.getPromoCodeSnapshot()
        );
    }
}
