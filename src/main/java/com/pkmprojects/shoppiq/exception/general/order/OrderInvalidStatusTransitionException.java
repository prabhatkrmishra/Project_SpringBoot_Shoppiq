package com.pkmprojects.shoppiq.exception.general.order;

import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when an invalid order status transition is attempted.
 *
 * <p>This exception is thrown when an admin attempts to move an order to
 * a status that is not reachable from the current status. The order
 * status machine defines valid transitions (e.g., PENDING to CONFIRMED,
 * CONFIRMED to SHIPPED). It uses the
 * {@link ErrorCode#ORDER_INVALID_STATUS_TRANSITION} code and HTTP 400
 * Bad Request status.</p>
 *
 * <p>The detail message includes the current and attempted statuses
 * (e.g., "Invalid status transition from SHIPPED to PENDING.") to help
 * the client understand which transition was invalid. The client should
 * review the order status machine and only attempt valid transitions.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ORDER_INVALID_STATUS_TRANSITION
 * @since 1.0.0
 */
public final class OrderInvalidStatusTransitionException extends InvalidOperationException {

    private OrderInvalidStatusTransitionException(String detail) {
        super(ErrorCode.ORDER_INVALID_STATUS_TRANSITION, detail);
    }

    /**
     * Creates an exception for an invalid status transition.
     *
     * @param current   the current order status
     * @param attempted the attempted new status
     * @return a new exception instance
     */
    public static OrderInvalidStatusTransitionException fromTo(OrderStatus current, OrderStatus attempted) {
        return new OrderInvalidStatusTransitionException(
                "Invalid status transition from %s to %s."
                        .formatted(current, attempted));
    }
}
