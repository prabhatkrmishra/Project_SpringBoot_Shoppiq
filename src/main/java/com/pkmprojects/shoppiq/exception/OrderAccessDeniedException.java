package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a user tries to access an order that does not belong to them.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class OrderAccessDeniedException extends UnauthorizedOperationException {

    public OrderAccessDeniedException(String detail) {
        super(ErrorCode.ORDER_ACCESS_DENIED, detail);
    }

    public static OrderAccessDeniedException forOrder(Long orderId) {
        return new OrderAccessDeniedException(
                "Order with id '%d' does not belong to you.".formatted(orderId)
        );
    }
}
