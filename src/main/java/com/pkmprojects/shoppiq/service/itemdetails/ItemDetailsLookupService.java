package com.pkmprojects.shoppiq.service.itemdetails;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;

import java.util.List;
import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Read-only item-details query facade.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * A <strong>ReadModel</strong> facade that decouples service-layer code from
 * {@code ItemDetailsRepository}. Provides lookup and aggregate queries for
 * inventory stock data.
 * </p>
 *
 * <h2>What are Item Details?</h2>
 * <p>
 * {@code ItemDetails} holds the <strong>variant-level</strong> product data: SKU, price,
 * stock quantity, brand, discount, category, and image. Each {@code Item} (product listing)
 * has one set of details.
 * </p>
 *
 * <h2>Consumers</h2>
 * <ul>
 *     <li>{@code CartService} — resolves item details for cart operations.</li>
 *     <li>{@code AdminInventoryServiceImpl} — admin inventory dashboards.</li>
 *     <li>{@code SellerInventoryServiceImpl} — seller inventory management.</li>
 *     <li>{@code AdminProductReadModelImpl} — admin product statistics.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 * @see ItemDetailsWriteService
 */
public interface ItemDetailsLookupService {

    /**
     * Finds item details by primary key.
     *
     * @param id the item-details identifier
     * @return matching item details, or empty if not found
     */
    Optional<ItemDetails> findById(Long id);

    /**
     * Returns the total number of item-details records.
     *
     * @return item-details count
     */
    long count();

    /**
     * Returns item-details with stock at or below the threshold (but greater than zero).
     *
     * @param threshold stock level threshold
     * @return low-stock item details
     */
    List<ItemDetails> findLowStockProducts(int threshold);

    /**
     * Returns item-details with zero stock.
     *
     * @return out-of-stock item details
     */
    List<ItemDetails> findOutOfStockProducts();

    /**
     * Counts item-details with stock at or below the threshold.
     *
     * @param threshold stock level threshold
     * @return count of low-stock items
     */
    long countLowStockProducts(int threshold);

    /**
     * Counts item-details with zero stock.
     *
     * @return count of out-of-stock items
     */
    long countOutOfStockProducts();

    /**
     * Returns low-stock item-details for a specific seller.
     *
     * @param threshold stock level threshold
     * @param sellerId  the seller identifier
     * @return low-stock item details owned by the seller
     */
    List<ItemDetails> findLowStockProductsBySellerId(int threshold, Long sellerId);

    /**
     * Returns out-of-stock item-details for a specific seller.
     *
     * @param sellerId the seller identifier
     * @return out-of-stock item details owned by the seller
     */
    List<ItemDetails> findOutOfStockProductsBySellerId(Long sellerId);

    /**
     * Counts low-stock item-details for a specific seller (BUG-007).
     */
    long countLowStockProductsBySellerId(int threshold, Long sellerId);

    /**
     * Counts out-of-stock item-details for a specific seller (BUG-007).
     */
    long countOutOfStockProductsBySellerId(Long sellerId);

    /**
     * Returns all item-details records.
     *
     * @return all item details
     */
    List<ItemDetails> findAll();
}
