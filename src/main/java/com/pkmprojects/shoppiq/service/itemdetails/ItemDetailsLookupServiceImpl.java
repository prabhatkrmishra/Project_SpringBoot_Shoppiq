package com.pkmprojects.shoppiq.service.itemdetails;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.repository.item.ItemDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Default implementation of {@link ItemDetailsLookupService}.
 *
 * <h2>What is {@code @Service}?</h2>
 * <p>
 * Registers this package-private class as a Spring bean.
 * </p>
 *
 * <h2>What is {@code @RequiredArgsConstructor}?</h2>
 * <p>
 * Lombok-generated constructor for injecting {@code ItemDetailsRepository}.
 * </p>
 *
 * <h2>What is {@code @Transactional(readOnly = true)}?</h2>
 * <p>
 * All queries are read-only, optimized for performance. The database can use
 * read-optimized plans without write-lock overhead.
 * </p>
 *
 * <p>All queries run in a read-only transaction. Delegates directly
 * to {@code ItemDetailsRepository}.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ItemDetailsLookupServiceImpl implements ItemDetailsLookupService {

    private final ItemDetailsRepository itemDetailsRepository;

    /**
     * Finds item details by ID.
     *
     * @param id item details ID
     * @return optional containing the item details if found
     */
    @Override
    public Optional<ItemDetails> findById(Long id) {
        return itemDetailsRepository.findById(id);
    }

    /**
     * Returns the total number of item details records.
     *
     * @return total count
     */
    @Override
    public long count() {
        return itemDetailsRepository.count();
    }

    /**
     * Finds item details with stock below the specified threshold.
     *
     * @param threshold the low-stock threshold
     * @return list of low-stock item details
     */
    @Override
    public List<ItemDetails> findLowStockProducts(int threshold) {
        return itemDetailsRepository.findLowStockProducts(threshold);
    }

    /**
     * Finds item details with zero stock.
     *
     * @return list of out-of-stock item details
     */
    @Override
    public List<ItemDetails> findOutOfStockProducts() {
        return itemDetailsRepository.findOutOfStockProducts();
    }

    /**
     * Counts item details with stock below the specified threshold.
     *
     * @param threshold the low-stock threshold
     * @return count of low-stock products
     */
    @Override
    public long countLowStockProducts(int threshold) {
        return itemDetailsRepository.countLowStockProducts(threshold);
    }

    /**
     * Counts item details with zero stock.
     *
     * @return count of out-of-stock products
     */
    @Override
    public long countOutOfStockProducts() {
        return itemDetailsRepository.countOutOfStockProducts();
    }

    /**
     * Finds low-stock item details for a specific seller.
     *
     * @param threshold the low-stock threshold
     * @param sellerId  the seller ID
     * @return list of low-stock item details for the seller
     */
    @Override
    public List<ItemDetails> findLowStockProductsBySellerId(int threshold, Long sellerId) {
        return itemDetailsRepository.findLowStockProductsBySellerId(threshold, sellerId);
    }

    /**
     * Finds out-of-stock item details for a specific seller.
     *
     * @param sellerId the seller ID
     * @return list of out-of-stock item details for the seller
     */
    @Override
    public List<ItemDetails> findOutOfStockProductsBySellerId(Long sellerId) {
        return itemDetailsRepository.findOutOfStockProductsBySellerId(sellerId);
    }

    /**
     * Counts low-stock item-details for a specific seller (BUG-007).
     */
    @Override
    public long countLowStockProductsBySellerId(int threshold, Long sellerId) {
        return itemDetailsRepository.countLowStockProductsBySellerId(threshold, sellerId);
    }

    /**
     * Counts out-of-stock item-details for a specific seller (BUG-007).
     */
    @Override
    public long countOutOfStockProductsBySellerId(Long sellerId) {
        return itemDetailsRepository.countOutOfStockProductsBySellerId(sellerId);
    }

    /**
     * Returns all item details records.
     *
     * @return list of all item details
     */
    @Override
    public List<ItemDetails> findAll() {
        return itemDetailsRepository.findAll();
    }

    /**
     * Returns the total stock quantity across all item details.
     *
     * @return sum of all stock quantities
     */
    @Override
    public long sumStockQuantity() {
        return itemDetailsRepository.sumStockQuantity();
    }

    /**
     * Returns the total inventory value (price * stock quantity) across all item details.
     *
     * @return total inventory value
     */
    @Override
    public java.math.BigDecimal sumInventoryValue() {
        return itemDetailsRepository.sumInventoryValue();
    }
}
