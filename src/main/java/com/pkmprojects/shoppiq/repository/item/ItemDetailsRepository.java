package com.pkmprojects.shoppiq.repository.item;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link ItemDetails} persistence operations.
 *
 * @author PrabhatKrMishra
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
}
