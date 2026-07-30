package com.pkmprojects.shoppiq.exception.general.order;

import com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a user tries to access an order that does not belong to them.
 *
 * <p>This exception is thrown when a customer attempts to access an order
 * that was placed by another user. Orders are private and can only be
 * viewed by the purchaser or an administrator. It uses the
 * {@link ErrorCode#ORDER_ACCESS_DENIED} code and HTTP 403 Forbidden
 * status.</p>
 *
 * <p>The detail message includes the order identifier (e.g.,
 * "Order with id '42' does not belong to you.") to help the client
 * understand which order was restricted. The client should ensure they
 * are accessing their own orders.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ORDER_ACCESS_DENIED
 * @since 1.0.0
 */
public final class OrderAccessDeniedException extends UnauthorizedOperationException {

    private OrderAccessDeniedException(String detail) {
        super(ErrorCode.ORDER_ACCESS_DENIED, detail);
    }

    /**
     * Creates an exception for a user attempting to access an order they do not own.
     *
     * @param orderId the order ID
     * @return a new exception instance
     */
    public static OrderAccessDeniedException forOrder(Long orderId) {
        return new OrderAccessDeniedException(
                "Order with id '%d' does not belong to you.".formatted(orderId)
        );
    }
}
