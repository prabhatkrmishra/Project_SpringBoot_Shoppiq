package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.request.*;
import com.pkmprojects.shoppiq.dto.admin.response.*;

import java.util.List;

/**
 * Business contract for admin inventory management.
 *
 * <p>
 * Defines the operations for managing product inventory,
 * including stock adjustments, bulk updates, and low stock
 * monitoring.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>List all products with inventory details.</li>
 *     <li>Adjust stock for a single product.</li>
 *     <li>Bulk update stock for multiple products.</li>
 *     <li>Get low stock and out of stock alerts.</li>
 *     <li>Get inventory dashboard summary.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Works exclusively with DTOs.</li>
 *     <li>Implemented by {@code AdminInventoryServiceImpl}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface AdminInventoryService {

    /**
     * Retrieves all products with inventory details.
     *
     * @return list of product inventory responses
     */
    List<AdminProductInventoryResponse> getAllProductInventory();

    /**
     * Retrieves products with low stock (below threshold but > 0).
     *
     * @return list of low stock products
     */
    List<AdminProductInventoryResponse> getLowStockProducts();

    /**
     * Retrieves products that are out of stock.
     *
     * @return list of out of stock products
     */
    List<AdminProductInventoryResponse> getOutOfStockProducts();

    /**
     * Adjusts stock for a single product.
     *
     * @param itemId  product identifier
     * @param request stock adjustment request
     * @return updated product inventory response
     */
    AdminProductInventoryResponse adjustStock(Long itemId, StockAdjustmentRequest request);

    /**
     * Bulk updates stock for multiple products.
     *
     * @param requests map of itemId to stock adjustment request
     * @return list of updated product inventory responses
     */
    List<AdminProductInventoryResponse> bulkUpdateStock(java.util.Map<Long, StockAdjustmentRequest> requests);

    /**
     * Retrieves inventory dashboard summary.
     *
     * @return inventory summary with counts
     */
    InventoryDashboardSummary getInventoryDashboardSummary();

    /**
     * Inventory dashboard summary data.
     */
    record InventoryDashboardSummary(
            long totalItems,
            long inStockItems,
            long lowStockItems,
            long outOfStockItems
    ) {
    }
}