package com.pkmprojects.shoppiq.events;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when an order's status transitions to a state requiring side effects.
 *
 * <p>Decouples the status update transaction from inventory restoration
 * and other post-transition side effects. This event is published when
 * an order reaches CANCELLED, RETURNED, or REFUNDED status, triggering
 * stock restoration in the inventory service.</p>
 *
 * <p>The event carries the order entity, the previous status, and the new
 * status. Listeners can use the status transition information to determine
 * which side effects need to be executed.</p>
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
