package com.pkmprojects.shoppiq.exception.general.order;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a seller attempts to modify an order that contains items from other sellers.
 *
 * <p>This exception is thrown when a seller tries to update the status of
 * an order that includes products from multiple sellers. A seller can
 * only modify orders that contain exclusively their own items. It uses
 * the {@link ErrorCode#INVALID_OPERATION} code and HTTP 400 Bad Request
 * status.</p>
 *
 * <p>The detail message includes the order identifier (e.g.,
 * "Order '42' contains items from other sellers and cannot be modified.")
 * to help the client understand why the modification was rejected. The
 * seller should only modify orders that contain only their products.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#INVALID_OPERATION
 * @since 1.0.0
 */
public final class OrderNotFullyOwnedException extends InvalidOperationException {

    private OrderNotFullyOwnedException(String detail) {
        super(ErrorCode.INVALID_OPERATION, detail);
    }

    /**
     * Creates an exception for an order containing items from other sellers.
     *
     * @param orderId the order ID
     * @return a new exception instance
     */
    public static OrderNotFullyOwnedException forOrder(Long orderId) {
        return new OrderNotFullyOwnedException(
                "Order '%d' contains items from other sellers and cannot be modified.".formatted(orderId)
        );
    }
}
