package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository responsible for {@link Item} persistence.
 *
 * <p>
 * Provides CRUD operations together with catalog-specific lookup
 * methods required by the business layer.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface ItemRepository
        extends JpaRepository<Item, Long> {

    /**
     * Finds all items owned by a specific seller.
     *
     * @param sellerId the seller identifier
     * @return list of items belonging to the seller
     */
    List<Item> findBySellerId(Long sellerId);

    Page<Item> findBySellerId(Long sellerId, Pageable pageable);

    /**
     * Counts the number of items owned by a specific seller.
     *
     * @param sellerId the seller identifier
     * @return count of items belonging to the seller
     */
    long countBySellerId(Long sellerId);

    /**
     * Finds an item by its identifier and seller ownership.
     *
     * @param id       the item identifier
     * @param sellerId the seller identifier
     * @return matching item if it belongs to the seller
     */
    Optional<Item> findByIdAndSellerId(Long id, Long sellerId);

    /**
     * Finds an item using its SKU.
     *
     * @param sku stock keeping unit
     * @return matching item if present
     */
    boolean existsByItemDetailsSku(String sku);

    /**
     * Determines whether another item already owns the supplied SKU.
     *
     * <p>
     * Used during update operations.
     * </p>
     *
     * @param sku SKU
     * @param id  item identifier to ignore
     * @return true if duplicate exists
     */
    boolean existsByItemDetailsSkuAndIdNot(
            String sku,
            Long id
    );

    /**
     * Retrieves all items ordered alphabetically.
     *
     * @return ordered item list
     */
    List<Item> findAllByOrderByNameAsc();

    Page<Item> findAllByOrderByNameAsc(Pageable pageable);

    /**
     * Retrieves all items with their item details eagerly fetched.
     *
     * @return list of items with item details
     */
    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemDetails id LEFT JOIN FETCH id.category")
    List<Item> findAllWithItemDetails();

    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemDetails id LEFT JOIN FETCH id.category")
    Page<Item> findAllWithItemDetails(Pageable pageable);

    Page<Item> findByPublishingStatus(ProductPublishingStatus status, Pageable pageable);

    /**
     * Returns the subset of the given SKUs that already exist in the database.
     *
     * @param skus set of SKUs to check
     * @return subset of SKUs that already exist
     */
    @Query("SELECT id.sku FROM ItemDetails id WHERE id.sku IN :skus")
    Set<String> findExistingSkus(@Param("skus") Set<String> skus);

    /**
     * Finds an item by its slug.
     *
     * @param slug URL-friendly identifier
     * @return matching item if present
     */
    Optional<Item> findBySlug(String slug);

    /**
     * Checks whether an item with the given slug exists.
     *
     * @param slug URL-friendly identifier
     * @return true if an item with this slug exists
     */
    boolean existsBySlug(String slug);

    /**
     * Retrieves the latest published items ordered by creation date.
     *
     * @param status publishing status filter
     * @param pageable pagination parameters
     * @return list of latest items
     */
    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemDetails id LEFT JOIN FETCH id.category " +
           "WHERE i.publishingStatus = :status ORDER BY i.createdAt DESC")
    List<Item> findNewArrivals(@Param("status") ProductPublishingStatus status, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemDetails id LEFT JOIN FETCH id.category " +
           "WHERE i.publishingStatus = :status ORDER BY i.createdAt DESC")
    Page<Item> findNewArrivalsPage(@Param("status") ProductPublishingStatus status, Pageable pageable);

    /**
     * Retrieves all published items that are marked as on sale.
     *
     * @return list of on-sale items
     */
    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemDetails id LEFT JOIN FETCH id.category " +
           "WHERE i.publishingStatus = 'PUBLISHED' AND id.onSale = true ORDER BY i.createdAt DESC")
    List<Item> findOnSaleItems();

    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemDetails id LEFT JOIN FETCH id.category " +
           "WHERE i.publishingStatus = 'PUBLISHED' AND id.onSale = true ORDER BY i.createdAt DESC")
    Page<Item> findOnSaleItemsPage(Pageable pageable);

    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemDetails id LEFT JOIN FETCH id.category " +
           "WHERE id.category.slug = :slug")
    Page<Item> findByCategorySlug(@Param("slug") String slug, Pageable pageable);
}