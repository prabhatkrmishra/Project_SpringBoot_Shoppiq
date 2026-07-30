package com.pkmprojects.shoppiq.events;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.promo.PromoCode;
import com.pkmprojects.shoppiq.entity.user.User;
import org.springframework.context.ApplicationEvent;

/**
 * Event published after a successful order is placed and persisted.
 *
 * <p>Decouples the checkout transaction from post-order side effects
 * such as email notifications, promo code usage recording, and analytics.
 * This event is published synchronously within the checkout transaction,
 * but listeners marked with {@code @Async} execute in a separate thread
 * to avoid blocking the checkout response.</p>
 *
 * <p>The event carries the newly created order, the authenticated user
 * who placed the order, and the promo code applied at checkout (if any).
 * Listeners can use this data to perform their side effects without
 * tightly coupling to the checkout service.</p>
 *
 * @author prabhatkrmishra
 * @see OrderPlacedEventListener
 * @since 1.4.0
 */
public class OrderPlacedEvent extends ApplicationEvent {

    private final Order order;
    private final User user;
    private final PromoCode appliedPromoCode;

    /**
     * Creates a new order-placed event.
     *
     * @param order            the newly created and persisted order
     * @param user             the customer who placed the order
     * @param appliedPromoCode the promo code applied at checkout, or
     *                         {@code null} if no promo was used
     */
    public OrderPlacedEvent(Order order, User user, PromoCode appliedPromoCode) {
        super(order);
        this.order = order;
        this.user = user;
        this.appliedPromoCode = appliedPromoCode;
    }

    /**
     * Returns the newly created order.
     *
     * @return the persisted order entity
     */
    public Order order() {
        return order;
    }

    /**
     * Returns the customer who placed the order.
     *
     * @return the authenticated user
     */
    public User user() {
        return user;
    }

    /**
     * Returns the promo code applied at checkout, if any.
     *
     * @return the applied promo code, or {@code null} if none was used
     */
    public PromoCode appliedPromoCode() {
        return appliedPromoCode;
    }
}
