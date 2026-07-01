package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.dto.seller.response.SellerInventoryResponse;
import com.pkmprojects.shoppiq.entity.User;

import java.util.List;

/**
 * Business contract for seller inventory management.
 *
 * <p>
 * Provides inventory-specific operations for sellers, including viewing
 * stock levels, identifying low stock and out-of-stock products, and
 * performing stock adjustments.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>List the seller's full inventory with stock status.</li>
 *     <li>Identify low stock products.</li>
 *     <li>Identify out of stock products.</li>
 *     <li>Adjust stock quantities for individual products.</li>
 *     <li>Enforce seller-level preconditions (ACTIVE, not SUSPENDED).</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface SellerInventoryService {

    /**
     * Retrieves the full inventory for the authenticated seller.
     *
     * @param user the authenticated user
     * @return list of seller's products with inventory info
     */
    List<SellerInventoryResponse> getInventory(User user);

    /**
     * Retrieves low stock products for the authenticated seller.
     *
     * @param user the authenticated user
     * @return list of low stock products
     */
    List<SellerInventoryResponse> getLowStockProducts(User user);

    /**
     * Retrieves out of stock products for the authenticated seller.
     *
     * @param user the authenticated user
     * @return list of out of stock products
     */
    List<SellerInventoryResponse> getOutOfStockProducts(User user);

    /**
     * Adjusts the stock quantity for a product owned by the seller.
     *
     * <p>The adjustment is relative: a positive value increases stock,
     * and a negative value decreases stock. The resulting quantity
     * must not be negative.</p>
     *
     * @param itemId   the product identifier
     * @param quantity the adjustment amount (positive or negative)
     * @param reason   the reason for adjustment
     * @param user     the authenticated user
     * @return updated inventory info for the product
     */
    SellerInventoryResponse adjustStock(Long itemId, int quantity, String reason, User user);
}
