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
 * Async event listener that handles post-status-change side effects for orders.
 *
 * <p>Sends status-change emails and restores inventory when the order
 * reaches CANCELLED, RETURNED, or REFUNDED. This listener is marked
 * with {@code @Async} to execute in a separate thread pool, ensuring
 * that the status update transaction is not blocked by email delivery
 * or inventory restoration.</p>
 *
 * <p>The listener reloads the order with its associations within the
 * async context to avoid LazyInitializationException. Failures in
 * email delivery or stock restoration are logged but do not propagate.</p>
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
