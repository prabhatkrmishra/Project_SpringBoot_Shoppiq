package com.pkmprojects.shoppiq.events;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.repository.order.OrderRepository;
import com.pkmprojects.shoppiq.service.inventory.InventoryService;
import com.pkmprojects.shoppiq.service.order.OrderEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * <strong>Spring Boot Concept:</strong> {@code @Async} event listener that
 * handles all post-status-change side effects for orders. Demonstrates
 * the <strong>Event Listener</strong> pattern with <strong>conditional
 * logic</strong> based on event payload.
 *
 * <p>Subscribes to {@link OrderStatusChangedEvent} and performs two
 * side effect operations that were previously embedded inline in
 * {@code AdminOrderServiceImpl} and {@code SellerOrderServiceImpl}:</p>
 * <ol>
 *     <li><strong>Status-change email</strong> — notifies the customer
 *         of the new order status via {@link OrderEmailService}.
 *         Runs for every status transition.</li>
 *     <li><strong>Stock restoration</strong> — restores inventory when
 *         the order reaches CANCELLED, RETURNED, or REFUNDED.</li>
 * </ol>
 *
 * <p><strong>Educational value:</strong> This listener builds on the
 * patterns shown in {@link OrderPlacedEventListener} and adds:
 * <ul>
 *   <li><strong>Re-fetching entities in async context</strong> — because
 *       this listener runs asynchronously, the original {@link com.pkmprojects.shoppiq.entity.order.Order}
 *       may be detached from the persistence context. The listener
 *       re-fetches it with the required associations eagerly loaded
 *       (via {@code orderRepository.findByIdWithUser()} and
 *       {@code orderRepository.findByIdWithItems()}) to avoid
 *       {@code LazyInitializationException}.</li>
 *   <li><strong>Conditional side effects</strong> — the
 *       {@code RESTORE_STOCK_STATUSES} set determines which statuses
 *       trigger stock restoration, keeping the logic declarative and
 *       easy to modify.</li>
 *   <li><strong>Multiple publishers, one listener</strong> — both
 *       {@code AdminOrderServiceImpl} and {@code SellerOrderServiceImpl}
 *       publish the same event type, and this single listener handles
 *       both, avoiding duplicated side-effect code.</li>
 * </ul>
 * </p>
 *
 * <h2>Async Execution</h2>
 * <p>This listener is annotated {@code @Async}, meaning it runs on a
 * separate thread from the status-update transaction. This ensures that:</p>
 * <ul>
 *     <li>Status update response time is not blocked by email SMTP calls.</li>
 *     <li>Email or stock-restore failures do not affect the status update.</li>
 * </ul>
 *
 * <h2>Error Handling</h2>
 * <p>Both operations are wrapped in try/catch blocks. Failures are
 * logged at WARN level and swallowed — the status update has already
 * been committed successfully at this point.</p>
 *
 * @author prabhatkrmishra
 * @see OrderStatusChangedEvent
 * @see InventoryService#restoreStock
 * @since 1.4.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusChangedEventListener {

    private static final Set<OrderStatus> RESTORE_STOCK_STATUSES = Set.of(
            OrderStatus.CANCELLED,
            OrderStatus.RETURNED,
            OrderStatus.REFUNDED
    );

    private final OrderEmailService orderEmailService;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    /**
     * Handles an {@link OrderStatusChangedEvent} asynchronously.
     *
     * <p>Sends a status-change email for every transition, then
     * restores stock if the new status requires it.</p>
     *
     * @param event the order-status-changed event
     */
    @Async
    @EventListener
    public void onStatusChanged(OrderStatusChangedEvent event) {
        sendEmail(event);
        if (RESTORE_STOCK_STATUSES.contains(event.newStatus())) {
            restoreStock(event.order().getId());
        }
    }

    /**
     * Sends a status-change email to the customer.
     *
     * <p>Reloads the order via {@link OrderRepository#findByIdWithUser(Long)} to
     * ensure the {@code User} association is eagerly fetched within the async
     * listener's persistence context. Without this, accessing {@code order.getUser()}
     * on a detached entity would throw {@code LazyInitializationException}.</p>
     *
     * @param event the order-status-changed event
     */
    private void sendEmail(OrderStatusChangedEvent event) {
        try {
            Order order = orderRepository.findByIdWithUser(event.order().getId()).orElse(null);
            if (order == null) {
                log.warn("Order {} not found — skipping status email", event.order().getId());
                return;
            }
            orderEmailService.sendOrderStatusEmail(order, event.newStatus());
            log.debug("Status email sent: orderId={}, status={}",
                    order.getId(), event.newStatus());
        } catch (Exception e) {
            log.warn("Failed to send status email for order {}: {}",
                    event.order().getId(), e.getMessage());
        }
    }

    /**
     * Reloads the order with items and restores stock via the inventory
     * service.
     *
     * @param orderId the order to restore stock for
     */
    private void restoreStock(Long orderId) {
        try {
            Order order = orderRepository.findByIdWithItems(orderId).orElse(null);
            if (order == null || order.getOrderItems().isEmpty()) {
                log.warn("Order {} not found or has no items — skipping stock restore", orderId);
                return;
            }
            inventoryService.restoreStock(order.getOrderItems());
            log.debug("Stock restored for order {} ({} item(s))",
                    orderId, order.getOrderItems().size());
        } catch (Exception e) {
            log.warn("Failed to restore stock for order {}: {}", orderId, e.getMessage());
        }
    }
}
