package com.pkmprojects.shoppiq.events;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.promo.PromoCode;
import com.pkmprojects.shoppiq.entity.user.User;
import org.springframework.context.ApplicationEvent;

/**
 * Published after a successful order is placed and persisted.
 *
 * <p>This event decouples the checkout transaction from post-order
 * side effects such as email notifications, promo code usage recording,
 * analytics, and inventory reconciliation.</p>
 *
 * <h2>Publisher</h2>
 * <p>Published by {@code CheckoutServiceImpl.doCheckout()} after the
 * {@code Order}, {@code OrderItem} snapshots, inventory reduction, and
 * cart clearing are all persisted within the checkout transaction. The
 * event is dispatched via Spring's {@code ApplicationEventPublisher},
 * which delivers it synchronously to non-async listeners and
 * asynchronously to {@code @Async} listeners.</p>
 *
 * <h2>Consumers</h2>
 * <ul>
 *     <li>{@link OrderPlacedEventListener} — sends order-placed email
 *         and records promo code usage asynchronously.</li>
 *     <li>Future: analytics, inventory reconciliation, recommendation
 *         engine triggers.</li>
 * </ul>
 *
 * <h2>Transaction Semantics</h2>
 * <p>The event source ({@code CheckoutServiceImpl}) runs inside a
 * {@code @Transactional} boundary. Listeners annotated with
 * {@code @TransactionalEventListener(phase = AFTER_COMMIT)} will
 * execute only after the checkout transaction commits. Listeners
 * annotated with {@code @Async} run on a separate thread and are
 * not subject to the source transaction's rollback.</p>
 *
 * @author PrabhatKrMishra
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
