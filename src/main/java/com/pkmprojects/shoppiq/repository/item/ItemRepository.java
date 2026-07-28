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
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository responsible for {@link Item} persistence.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Derived queries across associations</strong> — {@code findBySellerId},
 *       {@code countBySellerId}, {@code findByIdAndSellerId} navigate the {@code seller}
 *       relationship, generating {@code WHERE seller_id = ?}.</li>
 *   <li><strong>Nested property traversal</strong> — {@code existsByItemDetailsSku} resolves
 *       {@code Item → itemDetails → sku}, generating
 *       {@code SELECT ... FROM items i JOIN item_details id ON i.item_details_id = id.id WHERE id.sku = ?}.</li>
 *   <li><strong>Negated ID check</strong> — {@code existsByItemDetailsSkuAndIdNot} shows
 *       {@code IdNot} mapping to {@code id <> ?}.</li>
 *   <li><strong>JPQL with JOIN FETCH</strong> — {@code findAllWithItemDetails} eagerly loads
 *       {@code itemDetails} and {@code category} to avoid N+1. The {@code DISTINCT} keyword
 *       prevents duplicate rows from the join.</li>
 *   <li><strong>Enum filtering</strong> — {@code findByPublishingStatus} works with
 *       {@link com.pkmprojects.shoppiq.enums.ProductPublishingStatus} directly.</li>
 *   <li><strong>IN-clause queries</strong> — {@code findExistingSkus} uses
 *       {@code WHERE id.sku IN :skus} to batch-check which SKUs already exist.</li>
 *   <li><strong>Case-insensitive search</strong> — {@code findByNameContainingIgnoreCase}
 *       generates {@code WHERE LOWER(name) LIKE LOWER(CONCAT('%', ?, '%'))}.</li>
 *   <li><strong>Complex JPQL with multiple JOIN FETCHes</strong> — {@code findNewArrivals},
 *       {@code findOnSaleItems}, and {@code findByCategorySlug} combine eager fetching
 *       with filtering and sorting.</li>
 *   <li><strong>Native query with projection</strong> — {@code findTopSellingItemIds}
 *       executes raw SQL with aggregation ({@code SUM}), grouping, and returns
 *       {@link com.pkmprojects.shoppiq.repository.item.projection.ItemSalesRanking} projections.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findBySellerId(Long)
 *       → SELECT * FROM items WHERE seller_id = ?
 *   countBySellerId(Long)
 *       → SELECT COUNT(*) FROM items WHERE seller_id = ?
 *   findByIdAndSellerId(Long, Long)
 *       → SELECT * FROM items WHERE id = ? AND seller_id = ?
 *   existsByItemDetailsSku(String)
 *       → SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
 *         FROM items i JOIN item_details id ON i.item_details_id = id.id WHERE id.sku = ?
 *   findBySlug(String)
 *       → SELECT * FROM items WHERE slug = ?
 *   findByNameContainingIgnoreCase(String, Pageable)
 *       → SELECT * FROM items WHERE LOWER(name) LIKE LOWER(CONCAT('%', ?, '%'))
 *         ORDER BY ? LIMIT ? OFFSET ?
 * </pre>
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
     * Finds items whose name contains the given text (case-insensitive).
     *
     * <p>Used by the AI assistant's product-detail lookup as a fallback when no
     * exact slug match is found. The RAG retriever is the primary discovery path.</p>
     *
     * @param name partial product name
     * @param pageable pagination parameters
     * @return matching items
     */
    List<Item> findByNameContainingIgnoreCase(String name, Pageable pageable);

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

    /**
     * Returns item IDs ranked by total quantity sold in delivered orders
     * since the given date, limited to the specified count.
     *
     * @param since  cutoff timestamp (inclusive)
     * @param limit  max number of results
     * @return typed projections with itemId
     */
    @Query(value = "SELECT i.id AS item_id, SUM(oi.quantity) AS total_qty " +
           "FROM items i " +
           "JOIN order_items oi ON oi.item_details_id = i.item_details_id " +
           "JOIN orders o ON o.id = oi.order_id " +
           "WHERE o.status = 'DELIVERED' AND o.placed_at >= :since " +
           "GROUP BY i.id " +
           "ORDER BY total_qty DESC " +
           "LIMIT :limit",
           nativeQuery = true)
    List<ItemSalesRanking> findTopSellingItemIds(
            @Param("since") java.time.Instant since,
            @Param("limit") int limit
    );

}
