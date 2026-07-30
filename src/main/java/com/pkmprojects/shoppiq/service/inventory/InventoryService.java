package com.pkmprojects.shoppiq.service.inventory;

import com.pkmprojects.shoppiq.entity.cart.CartItem;
import com.pkmprojects.shoppiq.entity.order.OrderItem;
import com.pkmprojects.shoppiq.exception.general.inventory.InsufficientStockException;
import com.pkmprojects.shoppiq.exception.general.inventory.StockConflictException;

import java.util.List;

/**
 * Business contract for inventory stock operations.
 *
 * <p>Encapsulates stock validation, reduction during checkout, and restoration
 * on cancellation or return. Wraps {@code OptimisticLockingFailureException}
 * into {@link StockConflictException} for consistent error handling.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public interface InventoryService {

    /**
     * Reduces stock for every cart item, validating availability first.
     *
     * <p>Each item's stock quantity is decremented by the cart item's
     * requested quantity. The method iterates over all items and throws
     * on the first failure — the calling transaction will roll back all
     * changes.</p>
     *
     * <p>Optimistic locking failures (concurrent modification by another
     * customer) are caught and re-thrown as {@link StockConflictException}
     * with a user-friendly message.</p>
     *
     * @param cartItems the cart line items to reduce stock for;
     *                  each must have a non-null, loaded {@code ItemDetails}
     * @throws InsufficientStockException if any item's available stock is less
     *                                    than the requested quantity
     * @throws StockConflictException     if an optimistic lock conflict occurs
     *                                    during the save (concurrent inventory modification)
     */
    void reduceStock(List<CartItem> cartItems);

    /**
     * Restores stock for every order item after a cancelled or returned order.
     *
     * <p>Each item's stock quantity is incremented by the order item's
     * snapshotted quantity. Used when an order reaches a terminal
     * cancellation or return state (CANCELLED, RETURNED, REFUNDED).</p>
     *
     * @param orderItems the order line items to restore stock for;
     *                   each must have a non-null, loaded {@code ItemDetails}
     */
    void restoreStock(List<OrderItem> orderItems);
}
