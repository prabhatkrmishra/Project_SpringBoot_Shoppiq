package com.pkmprojects.shoppiq.service.item;

import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.repository.item.projection.ItemSalesRanking;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * <strong>Spring Boot Concept:</strong> Read-only item query facade.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * A <strong>ReadModel</strong> facade that decouples service-layer code from
 * {@code ItemRepository}. Provides lookup, search, paging, and aggregate queries
 * for product data without exposing the repository directly.
 * </p>
 *
 * <h2>Why a Separate Read Facade?</h2>
 * <ul>
 *   <li>Encapsulates all item read queries in one place.</li>
 *   <li>Services (like {@code ItemServiceImpl}, {@code AdminInventoryServiceImpl})
 *       depend on this interface, not on {@code ItemRepository} directly.</li>
 *   <li>Makes it easy to add caching, logging, or other cross-cutting concerns later.</li>
 * </ul>
 *
 * <p>Decouples service-layer code from {@code ItemRepository},
 * providing item lookup, search, paging, and aggregate queries.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public interface ItemLookupService {

    Optional<Item> findById(Long itemId);

    Optional<Item> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByItemDetailsSku(String sku);

    boolean existsByItemDetailsSkuAndIdNot(String sku, Long id);

    long count();

    long countBySellerId(Long sellerId);

    Page<Item> findAll(int page, int size);

    Page<Item> findBySellerId(Long sellerId, int page, int size);

    Page<Item> findByPublishingStatus(ProductPublishingStatus status, int page, int size);

    Optional<Item> findByIdAndSellerId(Long id, Long sellerId);

    List<Item> findByNameContaining(String name, int page, int size);

    List<Item> findNewArrivals(ProductPublishingStatus status, int page, int size);

    /**
     * Returns a page of newly published items, ordered by creation date descending.
     *
     * <p>Unlike {@link #findNewArrivals(ProductPublishingStatus, int, int)}, this
     * method returns a {@link Page} with accurate {@code totalElements} and
     * {@code totalPages} metadata for correct frontend pagination.</p>
     *
     * @param status the publishing status filter
     * @param page   zero-based page index
     * @param size   page size
     * @return a page of matching items with pagination metadata
     */
    Page<Item> findNewArrivalsPage(ProductPublishingStatus status, int page, int size);

    Page<Item> findOnSaleItemsPage(int page, int size);

    Page<Item> findByCategorySlug(String slug, int page, int size);

    List<ItemSalesRanking> findTopSellingItemIds(Instant since, int size);

    Set<String> findExistingSkus(Set<String> skus);

    List<Item> findAllByIds(List<Long> ids);

    Page<Item> findAllWithItemDetails(int page, int size);
}
