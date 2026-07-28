package com.pkmprojects.shoppiq.service.order;

import com.pkmprojects.shoppiq.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

/**
 * Single source of truth for order status transition rules.
 *
 * <p>Defines which status transitions are valid in the order lifecycle.
 * Both admin and seller order services must use this validator to ensure
 * consistent business rules across the application.</p>
 *
 * <h2>Valid State Machine</h2>
 * <pre>
 * PLACED → CONFIRMED → SHIPPED → OUT_FOR_DELIVERY → DELIVERED
 * PLACED → CANCEL_REQUEST → CANCELLED
 * PLACED → CANCELLED (direct)
 * DELIVERED → RETURN_REQUEST → RETURN_PICKUP → PICKUP_ARRIVED → RETURNED
 * DELIVERED → REFUND_REQUEST → RETURN_PICKUP → PICKUP_ARRIVED → REFUNDED
 * DELIVERED → REPLACE_REQUEST → REPLACE_PICKUP → PICKUP_ARRIVED → ISSUE_REPLACE → REPLACE_DELIVERED → REPLACED
 * </pre>
 *
 * @author Shoppiq
 * @since 1.4.0
 */
@Component
public class OrderStatusTransitionValidator {

    /**
     * Map of source status to the set of allowed target statuses.
     */
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.ofEntries(
            entry(OrderStatus.PLACED, Set.of(
                    OrderStatus.CONFIRMED,
                    OrderStatus.CANCEL_REQUEST,
                    OrderStatus.CANCELLED
            )),
            entry(OrderStatus.CANCEL_REQUEST, Set.of(
                    OrderStatus.CANCELLED
            )),
            entry(OrderStatus.CONFIRMED, Set.of(
                    OrderStatus.SHIPPED
            )),
            entry(OrderStatus.SHIPPED, Set.of(
                    OrderStatus.OUT_FOR_DELIVERY
            )),
            entry(OrderStatus.OUT_FOR_DELIVERY, Set.of(
                    OrderStatus.DELIVERED
            )),
            entry(OrderStatus.DELIVERED, Set.of(
                    OrderStatus.RETURN_REQUEST,
                    OrderStatus.REFUND_REQUEST,
                    OrderStatus.REPLACE_REQUEST
            )),
            entry(OrderStatus.RETURN_REQUEST, Set.of(
                    OrderStatus.RETURN_PICKUP
            )),
            entry(OrderStatus.REFUND_REQUEST, Set.of(
                    OrderStatus.RETURN_PICKUP
            )),
            entry(OrderStatus.REPLACE_REQUEST, Set.of(
                    OrderStatus.REPLACE_PICKUP
            )),
            entry(OrderStatus.RETURN_PICKUP, Set.of(
                    OrderStatus.PICKUP_ARRIVED
            )),
            entry(OrderStatus.PICKUP_ARRIVED, Set.of(
                    OrderStatus.RETURNED,
                    OrderStatus.REFUNDED,
                    OrderStatus.ISSUE_REPLACE
            )),
            entry(OrderStatus.REPLACE_PICKUP, Set.of(
                    OrderStatus.PICKUP_ARRIVED
            )),
            entry(OrderStatus.ISSUE_REPLACE, Set.of(
                    OrderStatus.REPLACE_DELIVERED
            )),
            entry(OrderStatus.REPLACE_DELIVERED, Set.of(
                    OrderStatus.REPLACED
            ))
    );

    /**
     * Terminal statuses that cannot transition to any other status.
     */
    private static final Set<OrderStatus> TERMINAL_STATUSES = Set.of(
            OrderStatus.CANCELLED,
            OrderStatus.RETURNED,
            OrderStatus.REFUNDED,
            OrderStatus.REPLACED
    );

    /**
     * Checks whether a status transition is allowed.
     *
     * @param from the current status
     * @param to   the desired new status
     * @return {@code true} if the transition is valid, {@code false} otherwise
     */
    public boolean isValidTransition(OrderStatus from, OrderStatus to) {
        if (from == to) {
            return false;
        }

        if (TERMINAL_STATUSES.contains(from)) {
            return false;
        }

        Set<OrderStatus> allowedTargets = TRANSITIONS.get(from);
        return allowedTargets != null && allowedTargets.contains(to);
    }
}
