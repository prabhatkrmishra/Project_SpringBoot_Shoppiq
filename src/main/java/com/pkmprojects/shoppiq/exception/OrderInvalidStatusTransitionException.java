package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when an invalid order status transition is attempted.
 *
 * <p>
 * This occurs when attempting to transition an order to a status that is not
 * allowed from its current status.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class OrderInvalidStatusTransitionException extends InvalidOperationException {

    public OrderInvalidStatusTransitionException(OrderStatus current, OrderStatus attempted) {
        super(ErrorCode.ORDER_INVALID_STATUS_TRANSITION,
                "Invalid status transition from %s to %s."
                        .formatted(current, attempted));
    }
}
