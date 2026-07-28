package com.pkmprojects.shoppiq.enums;

import com.pkmprojects.shoppiq.entity.order.Order;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * <strong>Spring Boot Concept:</strong> Lifecycle states of an {@link Order}.
 *
 * <p>The valid status transitions are defined once here and used by both
 * {@code AdminOrderServiceImpl} and {@code SellerOrderServiceImpl}.</p>
 *
 * <pre>
 * PLACED → CONFIRMED → SHIPPED → OUT_FOR_DELIVERY → DELIVERED
 * PLACED → CANCEL_REQUEST → CANCELLED
 * PLACED → CANCELLED (direct)
 * DELIVERED → RETURN_REQUEST → RETURN_PICKUP → PICKUP_ARRIVED → RETURNED
 * DELIVERED → REFUND_REQUEST → RETURN_PICKUP → PICKUP_ARRIVED → REFUNDED
 * DELIVERED → REPLACE_REQUEST → REPLACE_PICKUP → PICKUP_ARRIVED → ISSUE_REPLACE → REPLACE_DELIVERED → REPLACED
 * </pre>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>State machine pattern</strong> — Transitions are defined
 *         via a static {@code EnumMap<OrderStatus, Set<OrderStatus>>} that
 *         maps each status to its valid next states. The {@code canTransitionTo()}
 *         method encapsulates business rule validation in the enum itself,
 *         keeping it discoverable and testable.</li>
 *     <li><strong>Centralized transition logic</strong> — Both admin and
 *         seller order services use the same enum for status validation,
 *         ensuring consistency across the application.</li>
 *     <li><strong>Over 16 statuses</strong> — Demonstrates a complex state
 *         machine covering the full order lifecycle: placement, delivery,
 *         cancellation, return, refund, and replacement workflows.</li>
 *     <li><strong>Stored as STRING</strong> — Used with
 *         {@code @Enumerated(EnumType.STRING)} in the {@link Order} entity
 *         for human-readable database values.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum OrderStatus {

    PLACED,
    CONFIRMED,
    SHIPPED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    RETURNED,
    CANCEL_REQUEST,
    RETURN_REQUEST,
    REFUND_REQUEST,
    REFUNDED,
    RETURN_PICKUP,
    PICKUP_ARRIVED,
    REPLACE_PICKUP,
    REPLACE_REQUEST,
    ISSUE_REPLACE,
    REPLACE_DELIVERED,
    REPLACED;

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS;

    static {
        TRANSITIONS = new EnumMap<>(OrderStatus.class);
        TRANSITIONS.put(PLACED, EnumSet.of(CONFIRMED, CANCEL_REQUEST, CANCELLED));
        TRANSITIONS.put(CANCEL_REQUEST, EnumSet.of(CANCELLED));
        TRANSITIONS.put(CONFIRMED, EnumSet.of(SHIPPED));
        TRANSITIONS.put(SHIPPED, EnumSet.of(OUT_FOR_DELIVERY));
        TRANSITIONS.put(OUT_FOR_DELIVERY, EnumSet.of(DELIVERED));
        TRANSITIONS.put(DELIVERED, EnumSet.of(RETURN_REQUEST, REFUND_REQUEST, REPLACE_REQUEST));
        TRANSITIONS.put(RETURN_REQUEST, EnumSet.of(RETURN_PICKUP));
        TRANSITIONS.put(REFUND_REQUEST, EnumSet.of(RETURN_PICKUP));
        TRANSITIONS.put(REPLACE_REQUEST, EnumSet.of(REPLACE_PICKUP));
        TRANSITIONS.put(RETURN_PICKUP, EnumSet.of(PICKUP_ARRIVED));
        TRANSITIONS.put(PICKUP_ARRIVED, EnumSet.of(RETURNED, REFUNDED, ISSUE_REPLACE));
        TRANSITIONS.put(REPLACE_PICKUP, EnumSet.of(PICKUP_ARRIVED));
        TRANSITIONS.put(ISSUE_REPLACE, EnumSet.of(REPLACE_DELIVERED));
        TRANSITIONS.put(REPLACE_DELIVERED, EnumSet.of(REPLACED));
    }

    /**
     * Returns whether a transition from this status to the given target is allowed.
     *
     * @param target the desired target status
     * @return {@code true} if the transition is valid
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (this == target) {
            return true;
        }
        Set<OrderStatus> allowed = TRANSITIONS.get(this);
        return allowed == null || !allowed.contains(target);
    }
}
