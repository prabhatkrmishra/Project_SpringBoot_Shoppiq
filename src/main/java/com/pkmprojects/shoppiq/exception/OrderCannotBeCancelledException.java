package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.enums.OrderStatus;

/**
 * Exception thrown when a cancellation is attempted on an order
 * that is not in a cancellable state.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class OrderCannotBeCancelledException extends InvalidOperationException {

    public OrderCannotBeCancelledException(Long orderId, OrderStatus currentStatus) {
        super(ErrorCode.ORDER_CANNOT_BE_CANCELLED,
                "Order '%d' cannot be cancelled because it is already '%s'."
                        .formatted(orderId, currentStatus));
    }
}
