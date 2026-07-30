package com.pkmprojects.shoppiq.service.itemdetails;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;

import java.util.List;
import java.util.Optional;

/**
 * Read-only item-details query facade for lookup and aggregate queries.
 *
 * <p>Decouples service-layer code from {@code ItemDetailsRepository},
 * providing find, exists, count, and stock queries for item variant data.</p>
 *
 * @author prabhatkrmishra
 * @see ItemDetailsWriteService
 * @since 1.4.0
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

    /**
     * Returns the total stock quantity across all item details.
     *
     * @return sum of all stock quantities
     */
    long sumStockQuantity();

    /**
     * Returns the total inventory value (price * stock quantity) across all item details.
     *
     * @return total inventory value
     */
    java.math.BigDecimal sumInventoryValue();

    /**
     * Checks if any item details reference the given category.
     *
     * @param categoryId the category ID to check
     * @return true if items reference this category
     */
    boolean existsByCategoryId(Long categoryId);
}
