package com.pkmprojects.shoppiq.exception.general.order;

import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a cancellation is attempted on an order that is not in a cancellable state.
 *
 * <p>This exception is thrown when a customer or admin attempts to cancel
 * an order that is already shipped, delivered, or in a non-cancellable
 * state. It uses the {@link ErrorCode#ORDER_CANNOT_BE_CANCELLED} code
 * and HTTP 400 Bad Request status. Only orders in PENDING or CONFIRMED
 * status can be cancelled.</p>
 *
 * <p>The detail message includes the order ID and current status (e.g.,
 * "Order '42' cannot be cancelled because it is already 'SHIPPED'.")
 * to help the client understand why the cancellation was rejected. The
 * client should check the order status before attempting cancellation.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ORDER_CANNOT_BE_CANCELLED
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
