package com.pkmprojects.shoppiq.repository.item;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Persistence operations for the {@link ItemDetails} aggregate.
 *
 * <p>Provides methods to query inventory levels, stock quantities, and low-stock alerts for
 * inventory management. The repository supports aggregate queries for inventory reporting,
 * seller-specific stock queries, and existence checks for category dependency validation.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface ItemDetailsRepository extends JpaRepository<ItemDetails, Long> {

    /**
     * Finds products with stock quantity below or equal to the threshold (but > 0).
     *
     * @param threshold low stock threshold
     * @return list of item details with low stock
     */
    @Query("SELECT d FROM ItemDetails d WHERE d.stockQuantity > 0 AND d.stockQuantity <= :threshold")
    List<ItemDetails> findLowStockProducts(int threshold);

    /**
     * Finds products that are out of stock (quantity = 0).
     *
     * @return list of out of stock item details
     */
    @Query("SELECT d FROM ItemDetails d WHERE d.stockQuantity = 0")
    List<ItemDetails> findOutOfStockProducts();

    /**
     * Counts products with stock quantity at or below the given threshold (but > 0).
     *
     * @param threshold low stock threshold
     * @return count of low stock products
     */
    @Query("SELECT COUNT(d) FROM ItemDetails d WHERE d.stockQuantity > 0 AND d.stockQuantity <= :threshold")
    long countLowStockProducts(@Param("threshold") int threshold);

    /**
     * Counts products that are out of stock (quantity = 0).
     *
     * @return count of out of stock products
     */
    @Query("SELECT COUNT(d) FROM ItemDetails d WHERE d.stockQuantity = 0")
    long countOutOfStockProducts();

    /**
     * Returns all item-details with their {@code item} and {@code category}
     * associations eagerly fetched.
     *
     * <p>Prevents N+1 queries in inventory reports that access
     * {@code itemDetails.getItem().getName()} and
     * {@code itemDetails.getCategory().getName()}.</p>
     *
     * @return all item details with item and category loaded
     */
    @EntityGraph(attributePaths = {"item", "category"})
    @Override
    List<ItemDetails> findAll();

    /**
     * Finds low stock products belonging to a specific seller.
     *
     * @param threshold low stock threshold
     * @param sellerId  the seller identifier
     * @return list of low stock item details for the seller
     */
    @Query("SELECT d FROM ItemDetails d JOIN Item i ON i.itemDetails = d WHERE d.stockQuantity > 0 AND d.stockQuantity <= :threshold AND i.seller.id = :sellerId")
    List<ItemDetails> findLowStockProductsBySellerId(int threshold, Long sellerId);

    /**
     * Finds out of stock products belonging to a specific seller.
     *
     * @param sellerId the seller identifier
     * @return list of out of stock item details for the seller
     */
    @Query("SELECT d FROM ItemDetails d JOIN Item i ON i.itemDetails = d WHERE d.stockQuantity = 0 AND i.seller.id = :sellerId")
    List<ItemDetails> findOutOfStockProductsBySellerId(Long sellerId);

    /**
     * Counts low stock products belonging to a specific seller (BUG-007).
     */
    @Query("SELECT COUNT(d) FROM ItemDetails d JOIN Item i ON i.itemDetails = d WHERE d.stockQuantity > 0 AND d.stockQuantity <= :threshold AND i.seller.id = :sellerId")
    long countLowStockProductsBySellerId(@Param("threshold") int threshold, @Param("sellerId") Long sellerId);

    /**
     * Counts out of stock products belonging to a specific seller (BUG-007).
     */
    @Query("SELECT COUNT(d) FROM ItemDetails d JOIN Item i ON i.itemDetails = d WHERE d.stockQuantity = 0 AND i.seller.id = :sellerId")
    long countOutOfStockProductsBySellerId(@Param("sellerId") Long sellerId);

    /**
     * Returns the total stock quantity across all item details.
     *
     * @return sum of all stock quantities
     */
    @Query("SELECT COALESCE(SUM(d.stockQuantity), 0) FROM ItemDetails d")
    long sumStockQuantity();

    /**
     * Returns the total inventory value (price * stock quantity) across all item details.
     *
     * @return total inventory value
     */
    @Query("SELECT COALESCE(SUM(d.price * d.stockQuantity), 0) FROM ItemDetails d")
    java.math.BigDecimal sumInventoryValue();

    /**
     * Checks if any item details reference the given category.
     *
     * @param categoryId the category ID to check
     * @return true if items reference this category
     */
    boolean existsByCategoryId(Long categoryId);
}
