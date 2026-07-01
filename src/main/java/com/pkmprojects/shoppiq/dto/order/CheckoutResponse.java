package com.pkmprojects.shoppiq.dto.order;

import com.pkmprojects.shoppiq.entity.Order;
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
        BigDecimal grandTotal
) {

    /**
     * Constructs a {@link CheckoutResponse} from an {@link Order} entity.
     *
     * @param order the newly created order
     * @return checkout response
     */
    public static CheckoutResponse from(Order order) {
        return new CheckoutResponse(
                order.getId(),
                order.getStatus(),
                order.getGrandTotal()
        );
    }
}
