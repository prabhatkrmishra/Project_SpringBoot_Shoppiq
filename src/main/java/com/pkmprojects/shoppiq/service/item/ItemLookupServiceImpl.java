package com.pkmprojects.shoppiq.service.item;

import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.repository.item.ItemRepository;
import com.pkmprojects.shoppiq.repository.item.projection.ItemSalesRanking;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * {@link ItemLookupService} implementation providing read-only product queries.
 *
 * @author prabhatkrmishra
 * @see ItemLookupService
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ItemLookupServiceImpl implements ItemLookupService {

    private final ItemRepository itemRepository;

    /**
     * Finds an item by its database identifier.
     *
     * @param itemId the item ID
     * @return optional containing the item if found
     */
    @Override
    public Optional<Item> findById(Long itemId) {
        return itemRepository.findById(itemId);
    }

    /**
     * Finds an item by its URL-friendly slug.
     *
     * @param slug the item slug
     * @return optional containing the item if found
     */
    @Override
    public Optional<Item> findBySlug(String slug) {
        return itemRepository.findBySlug(slug);
    }

    /**
     * Checks whether an item exists with the given slug.
     *
     * @param slug the item slug
     * @return true if an item with that slug exists
     */
    @Override
    public boolean existsBySlug(String slug) {
        return itemRepository.existsBySlug(slug);
    }

    /**
     * Checks whether an item exists with the given SKU (via ItemDetails).
     *
     * @param sku the item SKU
     * @return true if an item with that SKU exists
     */
    @Override
    public boolean existsByItemDetailsSku(String sku) {
        return itemRepository.existsByItemDetailsSku(sku);
    }

    /**
     * Checks whether another item exists with the given SKU, excluding a specific item ID.
     *
     * @param sku the item SKU
     * @param id  the item ID to exclude
     * @return true if another item with that SKU exists
     */
    @Override
    public boolean existsByItemDetailsSkuAndIdNot(String sku, Long id) {
        return itemRepository.existsByItemDetailsSkuAndIdNot(sku, id);
    }

    /**
     * Returns the total number of items.
     *
     * @return total item count
     */
    @Override
    public long count() {
        return itemRepository.count();
    }

    /**
     * Counts items belonging to a specific seller.
     *
     * @param sellerId the seller ID
     * @return count of items for the seller
     */
    @Override
    public long countBySellerId(Long sellerId) {
        return itemRepository.countBySellerId(sellerId);
    }

    /**
     * Retrieves a paginated list of all items.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated item results
     */
    @Override
    public Page<Item> findAll(int page, int size) {
        return itemRepository.findAll(PageRequest.of(page, size));
    }

    /**
     * Finds a paginated list of items belonging to a seller.
     *
     * @param sellerId the seller ID
     * @param page     zero-based page index
     * @param size     page size
     * @return paginated item results
     */
    @Override
    public Page<Item> findBySellerId(Long sellerId, int page, int size) {
        return itemRepository.findBySellerId(sellerId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }

    /**
     * Finds a paginated list of items by publishing status.
     *
     * @param status the publishing status to filter by
     * @param page   zero-based page index
     * @param size   page size
     * @return paginated item results
     */
    @Override
    public Page<Item> findByPublishingStatus(ProductPublishingStatus status, int page, int size) {
        return itemRepository.findByPublishingStatus(status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }

    /**
     * Finds an item by ID that also belongs to the specified seller.
     *
     * @param id       the item ID
     * @param sellerId the seller ID
     * @return optional containing the item if found and owned by the seller
     */
    @Override
    public Optional<Item> findByIdAndSellerId(Long id, Long sellerId) {
        return itemRepository.findByIdAndSellerId(id, sellerId);
    }

    /**
     * Searches items by name containing the search term (case-insensitive).
     *
     * @param name the search term
     * @param page zero-based page index
     * @param size page size
     * @return list of matching items
     */
    @Override
    public List<Item> findByNameContaining(String name, int page, int size) {
        return itemRepository.findByNameContainingIgnoreCase(name,
                PageRequest.of(page, size));
    }

    /**
     * Finds new arrivals based on publishing status.
     *
     * @param status the publishing status to filter by
     * @param page   zero-based page index
     * @param size   page size
     * @return list of new arrival items
     */
    @Override
    public List<Item> findNewArrivals(ProductPublishingStatus status, int page, int size) {
        return itemRepository.findNewArrivals(status,
                PageRequest.of(page, size));
    }

    /**
     * Finds a paginated list of new arrivals based on publishing status.
     *
     * @param status the publishing status to filter by
     * @param page   zero-based page index
     * @param size   page size
     * @return paginated new arrival results
     */
    @Override
    public Page<Item> findNewArrivalsPage(ProductPublishingStatus status, int page, int size) {
        return itemRepository.findNewArrivalsPage(status,
                PageRequest.of(page, size));
    }

    /**
     * Finds a paginated list of items currently on sale.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated on-sale item results
     */
    @Override
    public Page<Item> findOnSaleItemsPage(int page, int size) {
        return itemRepository.findOnSaleItemsPage(PageRequest.of(page, size));
    }

    /**
     * Finds a paginated list of items by category slug.
     *
     * @param slug the category slug
     * @param page zero-based page index
     * @param size page size
     * @return paginated item results
     */
    @Override
    public Page<Item> findByCategorySlug(String slug, int page, int size) {
        return itemRepository.findByCategorySlug(slug, PageRequest.of(page, size));
    }

    /**
     * Finds top-selling item IDs based on order volume since the given timestamp.
     *
     * @param since start timestamp (inclusive)
     * @param size  maximum number of results
     * @return list of item sales ranking projections
     */
    @Override
    public List<ItemSalesRanking> findTopSellingItemIds(Instant since, int size) {
        return itemRepository.findTopSellingItemIds(since, size);
    }

    /**
     * Finds which SKUs from the given set already exist in the database.
     *
     * @param skus set of SKUs to check
     * @return set of SKUs that already exist
     */
    @Override
    public Set<String> findExistingSkus(Set<String> skus) {
        return itemRepository.findExistingSkus(skus);
    }

    /**
     * Finds all items by their IDs.
     *
     * @param ids list of item IDs
     * @return list of matching items
     */
    @Override
    public List<Item> findAllByIds(List<Long> ids) {
        return itemRepository.findAllById(ids);
    }

    /**
     * Finds a paginated list of all items with eagerly loaded item details.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated item results with item details
     */
    @Override
    public Page<Item> findAllWithItemDetails(int page, int size) {
        return itemRepository.findAllWithItemDetails(PageRequest.of(page, size));
    }
}
