package com.pkmprojects.shoppiq.service.inventory;

import com.pkmprojects.shoppiq.entity.cart.CartItem;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.order.OrderItem;
import com.pkmprojects.shoppiq.exception.general.inventory.InsufficientStockException;
import com.pkmprojects.shoppiq.exception.general.inventory.StockConflictException;
import com.pkmprojects.shoppiq.service.itemdetails.ItemDetailsWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of {@link InventoryService}.
 *
 * <p>Handles the stock-reduction side effect of placing an order.
 * Runs inside the caller's transaction so that stock changes are
 * atomic with the order creation.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Validates that requested quantity does not exceed available stock
 *         for each cart item.</li>
 *     <li>Reduces stock and persists the change via
 *         {@link ItemDetailsWriteService}.</li>
 *     <li>Catches {@link OptimisticLockingFailureException} (concurrent
 *         modification by another customer) and wraps it in a domain
 *         {@link StockConflictException}.</li>
 * </ul>
 *
 * <h2>Transaction Semantics</h2>
 * <p>This class is annotated {@code @Transactional} at the class level.
 * When called from {@code CheckoutServiceImpl.doCheckout()} (also
 * {@code @Transactional}), the call participates in the outer transaction.
 * If any stock check fails or an optimistic lock conflict occurs, the
 * entire checkout — including the order, order items, and stock changes —
 * is rolled back.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Slf4j
@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final ItemDetailsWriteService itemDetailsWriteService;

    public InventoryServiceImpl(ItemDetailsWriteService itemDetailsWriteService) {
        this.itemDetailsWriteService = itemDetailsWriteService;
    }

    @Override
    public void reduceStock(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            ItemDetails details = cartItem.getItemDetails();

            int newQuantity = details.getStockQuantity() - cartItem.getQuantity();
            if (newQuantity < 0) {
                throw InsufficientStockException.forItem(
                        details.getSku(), cartItem.getQuantity(), details.getStockQuantity());
            }
            details.setStockQuantity(newQuantity);
            try {
                itemDetailsWriteService.save(details);
            } catch (OptimisticLockingFailureException e) {
                throw StockConflictException.forOptimisticLock(
                        "Inventory was modified by another customer. Please refresh and try again.");
            }
        }
    }

    @Override
    public void restoreStock(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            ItemDetails details = orderItem.getItemDetails();
            if (details == null) {
                continue;
            }
            details.setStockQuantity(details.getStockQuantity() + orderItem.getQuantity());
            try {
                itemDetailsWriteService.save(details);
            } catch (OptimisticLockingFailureException e) {
                log.warn("Optimistic lock conflict restoring stock for SKU={}: {}",
                        details.getSku(), e.getMessage());
            }
        }
    }
}
