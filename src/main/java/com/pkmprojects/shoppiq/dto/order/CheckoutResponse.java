package com.pkmprojects.shoppiq.dto.order;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.enums.DeliveryType;
import com.pkmprojects.shoppiq.enums.OrderStatus;

import java.math.BigDecimal;

/**
 * Lightweight response returned immediately after a successful checkout.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record CheckoutResponse(

        Long orderId,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal deliveryCharge,
        BigDecimal codSurcharge,
        BigDecimal grandTotal,
        DeliveryType deliveryType,
        Long paymentId,
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
