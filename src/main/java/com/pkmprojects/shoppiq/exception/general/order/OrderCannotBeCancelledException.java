package com.pkmprojects.shoppiq.exception.general.order;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.enums.OrderStatus;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a cancellation
 * is attempted on an order that is not in a cancellable state.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) to enforce valid order cancellation states (e.g., cannot cancel
 * a SHIPPED order).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class OrderCannotBeCancelledException extends InvalidOperationException {

    private OrderCannotBeCancelledException(String detail) {
        super(ErrorCode.ORDER_CANNOT_BE_CANCELLED, detail);
    }

    /**
     * Creates an exception for an order that cannot be cancelled in its current state.
     *
     * @param orderId       the order ID
     * @param currentStatus the current order status
     * @return a new exception instance
     */
    public static OrderCannotBeCancelledException forOrder(Long orderId, OrderStatus currentStatus) {
        return new OrderCannotBeCancelledException(
                "Order '%d' cannot be cancelled because it is already '%s'."
                        .formatted(orderId, currentStatus));
    }
}
