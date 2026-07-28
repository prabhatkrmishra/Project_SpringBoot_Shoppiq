package com.pkmprojects.shoppiq.events;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import org.springframework.context.ApplicationEvent;

/**
 * <strong>Spring Boot Concept:</strong> Custom {@link org.springframework.context.ApplicationEvent}
 * published when an order's status transitions to a state that requires
 * side effects (e.g. stock restoration, refund processing).
 *
 * <p>This event decouples the status update transaction from inventory
 * restoration and other post-transition side effects.</p>
 *
 * <p><strong>Educational value:</strong> Contrast with {@link OrderPlacedEvent}:
 * <ul>
 *   <li><strong>Different event, same pattern</strong> — both events use
 *       Spring's {@code ApplicationEvent} mechanism for the same purpose:
 *       decoupling primary operations from side effects.</li>
 *   <li><strong>Multiple publishers</strong> — this event is published by
 *       both {@code AdminOrderServiceImpl} and {@code SellerOrderServiceImpl},
 *       demonstrating that one event type can have multiple publishers and
 *       multiple listeners.</li>
 *   <li><strong>Carries state transition data</strong> — unlike
 *       {@code OrderPlacedEvent} which carries the final state (PLACED),
 *       this event carries both the previous and new status, allowing
 *       listeners to react differently based on the type of transition.</li>
 *   <li><strong>Selective side effects</strong> — the listener
 *       ({@link OrderStatusChangedEventListener}) only restores stock for
 *       CANCELLED, RETURNED, and REFUNDED statuses, not for all transitions.</li>
 * </ul>
 * </p>
 *
 * <h2>Publisher</h2>
 * <p>Published by {@code AdminOrderServiceImpl} and
 * {@code SellerOrderServiceImpl} after a status transition is persisted.</p>
 *
 * <h2>Consumers</h2>
 * <ul>
 *     <li>{@link OrderStatusChangedEventListener} — sends status-change
 *         emails and restores inventory when the order reaches
 *         CANCELLED, RETURNED, or REFUNDED.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @see OrderStatusChangedEventListener
 * @since 1.4.0
 */
public class OrderStatusChangedEvent extends ApplicationEvent {

    private final Order order;
    private final OrderStatus previousStatus;
    private final OrderStatus newStatus;

    /**
     * Creates a new order-status-changed event.
     *
     * @param order          the order whose status changed
     * @param previousStatus the status before the transition
     * @param newStatus      the status after the transition
     */
    public OrderStatusChangedEvent(Order order, OrderStatus previousStatus, OrderStatus newStatus) {
        super(order);
        this.order = order;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    /**
     * Returns the order whose status changed.
     *
     * @return the order entity
     */
    public Order order() {
        return order;
    }

    /**
     * Returns the status before the transition.
     *
     * @return the previous status
     */
    public OrderStatus previousStatus() {
        return previousStatus;
    }

    /**
     * Returns the status after the transition.
     *
     * @return the new status
     */
    public OrderStatus newStatus() {
        return newStatus;
    }
}
