package com.pkmprojects.shoppiq.events;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.promo.PromoCode;
import com.pkmprojects.shoppiq.entity.user.User;
import org.springframework.context.ApplicationEvent;

/**
 * <strong>Spring Boot Concept:</strong> Custom {@link org.springframework.context.ApplicationEvent}
 * published after a successful order is placed and persisted. This is the
 * core of Spring's <strong>Event-Driven Design</strong> within the same
 * application context.
 *
 * <p>This event decouples the checkout transaction from post-order
 * side effects such as email notifications, promo code usage recording,
 * analytics, and inventory reconciliation.</p>
 *
 * <p><strong>Educational value:</strong> Spring's {@code ApplicationEvent}
 * mechanism is a lightweight, in-process event system that does not require
 * a message broker (no RabbitMQ, no Kafka). It is ideal for decoupling
 * primary business logic from side effects within the same application:
 * <ul>
 *   <li><strong>Loose coupling</strong> — {@code CheckoutServiceImpl} publishes
 *       events without knowing who listens or what they do.</li>
 *   <li><strong>Synchronous vs asynchronous</strong> — events are delivered
 *       synchronously by default, but listeners annotated with {@code @Async}
 *       execute on a separate thread (see {@link OrderPlacedEventListener}).</li>
 *   <li><strong>Transaction-aware listeners</strong> — using
 *       {@code @TransactionalEventListener(phase = AFTER_COMMIT)} would
 *       execute listeners only after the source transaction commits.</li>
 *   <li><strong>Event as data carrier</strong> — this class carries the
 *       {@link com.pkmprojects.shoppiq.entity.order.Order},
 *       {@link com.pkmprojects.shoppiq.entity.user.User}, and optional
 *       {@link com.pkmprojects.shoppiq.entity.promo.PromoCode} so listeners
 *       have all the context they need without querying the database again.</li>
 * </ul>
 * </p>
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
