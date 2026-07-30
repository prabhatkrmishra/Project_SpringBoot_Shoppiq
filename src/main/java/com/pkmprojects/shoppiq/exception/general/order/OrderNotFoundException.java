package com.pkmprojects.shoppiq.exception.general.order;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when the requested order cannot be found.
 *
 * <p>This exception is thrown by order service methods when a database
 * lookup for an order fails. It uses the {@link ErrorCode#ORDER_NOT_FOUND}
 * code and HTTP 404 Not Found status. The order ID may be incorrect or
 * the order may have been soft-deleted.</p>
 *
 * <p>The detail message includes the order identifier (e.g.,
 * "Order with id '42' was not found.") to help the client understand
 * which order was invalid. The client should verify the order ID and
 * retry the request.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ORDER_NOT_FOUND
 * @since 1.0.0
 */
public final class OrderNotFoundException extends ResourceNotFoundException {

    /**
     * Creates a new OrderNotFoundException.
     *
     * @param detail detailed error description
     */
    public OrderNotFoundException(String detail) {
        super(ErrorCode.ORDER_NOT_FOUND, detail);
    }

    /**
     * Creates an exception for a missing order by its identifier.
     *
     * @param orderId the missing order's identifier
     * @return a new OrderNotFoundException
     */
    public static OrderNotFoundException id(Long orderId) {
        return new OrderNotFoundException(
                "Order with id '%d' was not found.".formatted(orderId)
        );
    }
}
