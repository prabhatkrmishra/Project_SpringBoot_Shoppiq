package com.pkmprojects.shoppiq.service.inventory;

import com.pkmprojects.shoppiq.entity.cart.CartItem;
import com.pkmprojects.shoppiq.entity.order.OrderItem;
import com.pkmprojects.shoppiq.exception.general.inventory.InsufficientStockException;
import com.pkmprojects.shoppiq.exception.general.inventory.StockConflictException;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Contract for inventory stock operations.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * This is a focused <strong>Service layer</strong> interface that encapsulates stock management
 * as a reusable service. It is primarily consumed by {@code CheckoutServiceImpl} during
 * order placement.
 * </p>
 *
 * <h2>Why a Separate Inventory Service?</h2>
 * <ul>
 *   <li>Encapsulates the "reduce stock" side effect of placing an order.</li>
 *   <li>Provides a single place for stock validation and optimistic locking handling.</li>
 *   <li>Keeps the checkout service focused on orchestration, not inventory math.</li>
 * </ul>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Validates that requested quantity does not exceed available stock.</li>
 *     <li>Reduces stock atomically for all items in a single transaction.</li>
 *     <li>Restores stock when orders are cancelled or returned.</li>
 *     <li>Wraps {@code OptimisticLockingFailureException} into a domain
 *         {@link StockConflictException} for consistent error handling.</li>
 * </ul>
 *
 * <p>Encapsulates stock validation and reduction so that checkout
 * and other order workflows do not reach directly into the
 * {@code ItemDetailsRepository}. This service owns the "reduce stock"
 * side effect of placing an order.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Validates that requested quantity does not exceed available stock.</li>
 *     <li>Reduces stock atomically for all items in a single transaction.</li>
 *     <li>Wraps {@code OptimisticLockingFailureException} into a domain
 *         {@link StockConflictException} for consistent error handling.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Accepts already-loaded {@link CartItem} entities to avoid redundant
 *         queries within the calling transaction.</li>
 *     <li>Called by {@code CheckoutServiceImpl} after the {@code Order} and
 *         {@code OrderItem} snapshots are persisted but before the cart is
 *         cleared, ensuring stock is decremented in the same transaction.</li>
 * </ul>
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
