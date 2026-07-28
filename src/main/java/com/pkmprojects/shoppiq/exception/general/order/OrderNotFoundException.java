package com.pkmprojects.shoppiq.exception.general.order;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when the requested
 * order cannot be found.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) for missing {@link com.pkmprojects.shoppiq.entity.order.Order}
 * entities. Unlike most project exceptions using private constructors, this
 * one has a public constructor for flexible message creation in addition to
 * the factory method.</p>
 *
 * @author prabhatkrmishra
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
