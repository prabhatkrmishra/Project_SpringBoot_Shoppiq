package com.pkmprojects.shoppiq.repository.item;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link ItemDetails} persistence operations.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Custom JPQL queries</strong> — Methods like {@code findLowStockProducts}
 *       and {@code findOutOfStockProducts} use {@code @Query} with JPQL to express
 *       conditions ({@code stockQuantity > 0 AND stockQuantity <= :threshold}) that
 *       derived method names cannot cleanly represent.</li>
 *   <li><strong>Custom JPQL count queries</strong> — {@code countLowStockProducts} and
 *       {@code countOutOfStockProducts} use {@code SELECT COUNT(d)} for efficient
 *       aggregation.</li>
 *   <li><strong>{@code @EntityGraph}</strong> — The overridden {@link #findAll()} method
 *       uses {@code @EntityGraph(attributePaths = {"item", "category"})} to eagerly fetch
 *       associations via a SQL {@code LEFT OUTER JOIN}, preventing N+1 queries.</li>
 *   <li><strong>JPQL with manual JOIN</strong> — {@code findLowStockProductsBySellerId}
 *       joins {@code ItemDetails} to {@code Item} via {@code JOIN Item i ON i.itemDetails = d}
 *       to filter by seller ownership.</li>
 * </ul>
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
     * @param sellerId the seller identifier
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
}
