package com.pkmprojects.shoppiq.exception.general.order;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a seller
 * attempts to modify an order that contains items from other sellers.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) to enforce multi-tenant ownership — sellers can only act on
 * their own line items.</p>
 *
 * @author prabhatkrmishra
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
