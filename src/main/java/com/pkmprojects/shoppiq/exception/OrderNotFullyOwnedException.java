package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a seller attempts to modify an order
 * that contains items from other sellers.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class OrderNotFullyOwnedException extends InvalidOperationException {

    public OrderNotFullyOwnedException(String detail) {
        super(ErrorCode.INVALID_OPERATION, detail);
    }

    public static OrderNotFullyOwnedException forOrder(Long orderId) {
        return new OrderNotFullyOwnedException(
                "Order '%d' contains items from other sellers and cannot be modified.".formatted(orderId)
        );
    }
}
