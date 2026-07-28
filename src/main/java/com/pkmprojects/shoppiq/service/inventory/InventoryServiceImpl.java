package com.pkmprojects.shoppiq.service.inventory;

import com.pkmprojects.shoppiq.entity.cart.CartItem;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.order.OrderItem;
import com.pkmprojects.shoppiq.events.inventory.StockRestoreFailedEvent;
import com.pkmprojects.shoppiq.exception.general.inventory.InsufficientStockException;
import com.pkmprojects.shoppiq.exception.general.inventory.StockConflictException;
import com.pkmprojects.shoppiq.service.itemdetails.ItemDetailsWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Implementation of {@link InventoryService}
 * containing stock adjustment business logic.
 *
 * <p>Reduces stock during checkout and restores stock on cancellation or return.
 * Runs inside the caller's transaction via Spring's REQUIRED propagation,
 * ensuring stock changes are atomic with order operations. Handles
 * {@code OptimisticLockingFailureException} for concurrent access.</p>
 *
 * <p>Why this design:
 * <ul>
 *   <li><strong>@Service</strong> — Spring stereotype for service-layer beans, auto-detected via component scanning.</li>
 *   <li><strong>@Transactional</strong> — Designed to participate in the outer checkout transaction via REQUIRED propagation.</li>
 *   <li><strong>Constructor injection</strong> — final fields for immutability and testability.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see InventoryService
 * @since 1.4.0
 */
@Slf4j
@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final ItemDetailsWriteService itemDetailsWriteService;
    private final ApplicationEventPublisher eventPublisher;

    public InventoryServiceImpl(ItemDetailsWriteService itemDetailsWriteService,
                                ApplicationEventPublisher eventPublisher) {
        this.itemDetailsWriteService = itemDetailsWriteService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Reduces stock for each cart item during checkout.
     *
     * <p>Validates sufficient stock before deducting. Handles
     * optimistic locking failures for concurrent access.</p>
     *
     * @param cartItems list of cart items to deduct stock from
     * @throws InsufficientStockException if stock is insufficient
     * @throws StockConflictException     if a concurrent modification is detected
     */
    @Override
    public void reduceStock(List<CartItem> cartItems) {
        // Phase 1: validate stock for all items and prepare modifications
        List<ItemDetails> modified = new java.util.ArrayList<>(cartItems.size());
        for (CartItem cartItem : cartItems) {
            ItemDetails details = cartItem.getItemDetails();
            int newQuantity = details.getStockQuantity() - cartItem.getQuantity();
            if (newQuantity < 0) {
                throw InsufficientStockException.forItem(
                        details.getSku(), cartItem.getQuantity(), details.getStockQuantity());
            }
            details.setStockQuantity(newQuantity);
            modified.add(details);
        }

        // Phase 2: batch persist in a single round-trip
        try {
            itemDetailsWriteService.saveAll(modified);
        } catch (OptimisticLockingFailureException _) {
            throw StockConflictException.forOptimisticLock(
                    "Inventory was modified by another customer. Please refresh and try again.");
        }
    }

    /**
     * Restores stock for each order item upon cancellation or return.
     *
     * <p>Skips items with null item details. Applies all restorations
     * in a single batch to minimise database round-trips. Logs an error
     * on optimistic locking conflicts and throws a {@link StockConflictException}
     * so the caller can fail the operation.</p>
     *
     * @param orderItems list of order items to restore stock for
     * @throws StockConflictException if an optimistic locking conflict is detected
     */
    @Override
    public void restoreStock(List<OrderItem> orderItems) {
        List<ItemDetails> toUpdate = new java.util.ArrayList<>(orderItems.size());
        for (OrderItem orderItem : orderItems) {
            ItemDetails details = orderItem.getItemDetails();
            if (details == null) {
                continue;
            }
            details.setStockQuantity(details.getStockQuantity() + orderItem.getQuantity());
            toUpdate.add(details);
        }

        if (toUpdate.isEmpty()) {
            return;
        }

        try {
            itemDetailsWriteService.saveAll(toUpdate);
        } catch (OptimisticLockingFailureException e) {
            String skus = toUpdate.stream()
                    .map(ItemDetails::getSku)
                    .collect(java.util.stream.Collectors.joining(", "));
            String message = "Failed to restore stock for SKUs=[" + skus
                    + "]. Please retry: " + e.getMessage();
            log.error("Optimistic lock conflict restoring stock for SKUs=[{}]: {}",
                    skus, e.getMessage(), e);
            eventPublisher.publishEvent(new StockRestoreFailedEvent(
                    this,
                    skus,
                    null,
                    toUpdate.size(),
                    message
            ));
            throw StockConflictException.forOptimisticLock(message);
        }
    }
}
