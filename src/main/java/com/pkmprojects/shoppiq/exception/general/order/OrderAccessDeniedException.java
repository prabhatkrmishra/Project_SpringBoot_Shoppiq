package com.pkmprojects.shoppiq.exception.general.order;

import com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a user tries
 * to access an order that does not belong to them.
 *
 * <p>Leaf exception in the authorization hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException}
 * (HTTP 403) for order ownership violations.</p>
 *
 * @author prabhatkrmishra
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
