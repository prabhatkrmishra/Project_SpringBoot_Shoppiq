package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when the requested Order cannot be found.
 *
 * @author PrabhatKrMishra
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