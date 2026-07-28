package com.pkmprojects.shoppiq.exception.general.order;

import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when an invalid
 * order status transition is attempted.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) to enforce the order lifecycle state machine (e.g., cannot go
 * from DELIVERED back to PENDING).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class OrderInvalidStatusTransitionException extends InvalidOperationException {

    private OrderInvalidStatusTransitionException(String detail) {
        super(ErrorCode.ORDER_INVALID_STATUS_TRANSITION, detail);
    }

    /**
     * Creates an exception for an invalid status transition.
     *
     * @param current   the current order status
     * @param attempted the attempted new status
     * @return a new exception instance
     */
    public static OrderInvalidStatusTransitionException fromTo(OrderStatus current, OrderStatus attempted) {
        return new OrderInvalidStatusTransitionException(
                "Invalid status transition from %s to %s."
                        .formatted(current, attempted));
    }
}
