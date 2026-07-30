package com.pkmprojects.shoppiq.repository.item;

import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.repository.item.projection.ItemSalesRanking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Persistence operations for the {@link Item} aggregate.
 *
 * <p>Provides methods to query items by seller, category, publishing status, and various filters
 * for catalog management. The repository supports paginated queries for item listing, eager
 * fetching of item details and categories, and aggregate queries for sales analytics and
 * product performance reporting.</p>
 *
 * @author prabhatkrmishra
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

    /**
     * Returns a paginated view of items owned by a specific seller.
     *
     * @param sellerId the seller identifier
     * @param pageable pagination parameters
     * @return paginated list of items
     */
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

    /**
     * Returns a paginated view of all items ordered alphabetically.
     *
     * @param pageable pagination parameters
     * @return paginated list of items
     */
    Page<Item> findAllByOrderByNameAsc(Pageable pageable);

    /**
     * Retrieves all items with their item details eagerly fetched.
     *
     * @return list of items with item details
     */
    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemDetails id LEFT JOIN FETCH id.category")
    List<Item> findAllWithItemDetails();

    /**
     * Returns a paginated view of all items with item details eagerly fetched.
     *
     * @param pageable pagination parameters
     * @return paginated list of items with item details
     */
    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemDetails id LEFT JOIN FETCH id.category")
    Page<Item> findAllWithItemDetails(Pageable pageable);

    /**
     * Returns a paginated view of items filtered by publishing status.
     *
     * @param status   the publishing status
     * @param pageable pagination parameters
     * @return paginated list of items
     */
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
     * Finds items whose name contains the given text (case-insensitive).
     *
     * <p>Used by the AI assistant's product-detail lookup as a fallback when no
     * exact slug match is found. The RAG retriever is the primary discovery path.</p>
     *
     * @param name     partial product name
     * @param pageable pagination parameters
     * @return matching items
     */
    List<Item> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Retrieves the latest published items ordered by creation date.
     *
     * @param status   publishing status filter
     * @param pageable pagination parameters
     * @return list of latest items
     */
    @Query("""
            SELECT DISTINCT i FROM Item i
            LEFT JOIN FETCH i.itemDetails id
            LEFT JOIN FETCH id.category
            WHERE i.publishingStatus = :status
            ORDER BY i.createdAt DESC""")
    List<Item> findNewArrivals(@Param("status") ProductPublishingStatus status, org.springframework.data.domain.Pageable pageable);

    /**
     * Returns a paginated view of the latest published items ordered by creation date.
     *
     * @param status   publishing status filter
     * @param pageable pagination parameters
     * @return paginated list of latest items
     */
    @Query("""
            SELECT DISTINCT i FROM Item i
            LEFT JOIN FETCH i.itemDetails id
            LEFT JOIN FETCH id.category
            WHERE i.publishingStatus = :status
            ORDER BY i.createdAt DESC""")
    Page<Item> findNewArrivalsPage(@Param("status") ProductPublishingStatus status, Pageable pageable);

    /**
     * Retrieves all published items that are marked as on sale.
     *
     * @return list of on-sale items
     */
    @Query("""
            SELECT DISTINCT i FROM Item i
            LEFT JOIN FETCH i.itemDetails id
            LEFT JOIN FETCH id.category
            WHERE i.publishingStatus = 'PUBLISHED' AND id.onSale = true
            ORDER BY i.createdAt DESC""")
    List<Item> findOnSaleItems();

    /**
     * Returns a paginated view of all published items that are marked as on sale.
     *
     * @param pageable pagination parameters
     * @return paginated list of on-sale items
     */
    @Query("""
            SELECT DISTINCT i FROM Item i
            LEFT JOIN FETCH i.itemDetails id
            LEFT JOIN FETCH id.category
            WHERE i.publishingStatus = 'PUBLISHED' AND id.onSale = true
            ORDER BY i.createdAt DESC""")
    Page<Item> findOnSaleItemsPage(Pageable pageable);

    /**
     * Returns a paginated view of items belonging to a category identified by slug.
     *
     * @param slug     the category URL slug
     * @param pageable pagination parameters
     * @return paginated list of items in the category
     */
    @Query("""
            SELECT DISTINCT i FROM Item i
            LEFT JOIN FETCH i.itemDetails id
            LEFT JOIN FETCH id.category
            WHERE id.category.slug = :slug""")
    Page<Item> findByCategorySlug(@Param("slug") String slug, Pageable pageable);

    /**
     * Returns item IDs ranked by total quantity sold in delivered orders
     * since the given date, limited to the specified count.
     *
     * @param since cutoff timestamp (inclusive)
     * @param limit max number of results
     * @return typed projections with itemId
     */
    @Query(value = """
            SELECT i.id AS item_id, SUM(oi.quantity) AS total_qty
            FROM items i
            JOIN order_items oi ON oi.item_details_id = i.item_details_id
            JOIN orders o ON o.id = oi.order_id
            WHERE o.status = 'DELIVERED' AND o.placed_at >= :since
            GROUP BY i.id
            ORDER BY total_qty DESC
            LIMIT :limit""",
            nativeQuery = true)
    List<ItemSalesRanking> findTopSellingItemIds(
            @Param("since") java.time.Instant since,
            @Param("limit") int limit
    );

}
