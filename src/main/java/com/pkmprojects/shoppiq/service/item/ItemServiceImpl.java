package com.pkmprojects.shoppiq.service.item;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.item.ItemRequest;
import com.pkmprojects.shoppiq.dto.item.ItemResponse;
import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.exception.business.SlugGenerationFailedException;
import com.pkmprojects.shoppiq.exception.general.category.CategoryNotFoundException;
import com.pkmprojects.shoppiq.exception.general.item.DuplicateItemException;
import com.pkmprojects.shoppiq.exception.general.item.ItemNotFoundException;
import com.pkmprojects.shoppiq.service.category.CategoryLookupService;
import com.pkmprojects.shoppiq.util.SlugUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link ItemService} implementation handling bulk item import with SKU uniqueness
 * validation, category resolution, unique slug generation with retry-on-conflict,
 * and catalog queries.
 *
 * @author prabhatkrmishra
 * @see ItemService
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemLookupService itemLookupService;
    private final ItemWriteService itemWriteService;
    private final CategoryLookupService categoryLookupService;
    private final Clock clock;

    public ItemServiceImpl(
            ItemLookupService itemLookupService,
            ItemWriteService itemWriteService,
            CategoryLookupService categoryLookupService,
            Clock clock
    ) {
        this.itemLookupService = itemLookupService;
        this.itemWriteService = itemWriteService;
        this.categoryLookupService = categoryLookupService;
        this.clock = clock;
    }

    private static String extractRootMessage(DataIntegrityViolationException e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause.getMessage() != null) {
                return cause.getMessage();
            }
            cause = cause.getCause();
        }
        return null;
    }

    private Item buildItem(ItemRequest request, Category category) {
        ItemDetails itemDetails = ItemDetails.builder()
                .brand(request.brand())
                .sku(request.sku())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .discountPercentage(request.discountPercentage())
                .imageUrl(request.imageUrl())
                .category(category)
                .build();

        Item item = Item.builder()
                .name(request.name())
                .slug(generateUniqueSlug(request.name()))
                .description(request.description())
                .itemDetails(itemDetails)
                .build();

        itemDetails.setItem(item);
        return item;
    }

    private void saveWithSlugRetry(Item item) {
        int attempts = 0;
        while (attempts < 10) {
            try {
                itemWriteService.save(item);
                return;
            } catch (DataIntegrityViolationException e) {
                String rootMessage = extractRootMessage(e);
                if (rootMessage != null && rootMessage.toLowerCase().contains("slug")) {
                    item.setSlug(generateUniqueSlug(item.getName()));
                    attempts++;
                } else {
                    throw e;
                }
            }
        }
        throw SlugGenerationFailedException.forEntity("item", item.getName(), 10);
    }

    private Item findItem(Long id) {
        return itemLookupService.findById(id)
                .orElseThrow(() -> ItemNotFoundException.id(id));
    }

    /**
     * Imports multiple items in bulk with SKU uniqueness validation and
     * atomic slug-generation retry on conflict.
     *
     * <p>Validates that no request-level SKU duplicates exist, all referenced
     * categories exist, and no SKU already exists in the database. Each item
     * is persisted individually with slug retry logic. Evicts all entries
     * from the "items" cache after successful creation to ensure fresh
     * data on subsequent reads.</p>
     *
     * @param requests list of item creation requests
     * @return list of created item responses
     * @throws DuplicateItemException    if a duplicate SKU is detected
     * @throws CategoryNotFoundException if a referenced category is not found
     */
    @Override
    @Transactional
    @CacheEvict(value = "items", allEntries = true)
    public List<ItemResponse> createBulk(List<ItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        Set<String> requestSkus = new HashSet<>();
        for (ItemRequest request : requests) {
            if (!requestSkus.add(request.sku())) {
                throw DuplicateItemException.sku(request.sku());
            }
        }

        Set<Long> categoryIds = requests.stream()
                .map(ItemRequest::categoryId)
                .collect(Collectors.toSet());

        Map<Long, Category> categories = categoryLookupService
                .findAllByIds(categoryIds.stream().toList())
                .stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        if (categories.size() != categoryIds.size()) {
            throw CategoryNotFoundException.ids();
        }

        Set<String> existingSkus = itemLookupService.findExistingSkus(requestSkus);
        if (!existingSkus.isEmpty()) {
            throw DuplicateItemException.sku(existingSkus.iterator().next());
        }

        List<Item> items = requests.stream()
                .map(request -> buildItem(request, categories.get(request.categoryId())))
                .toList();

        for (Item item : items) {
            saveWithSlugRetry(item);
        }

        return items.stream()
                .map(ItemResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieves a single item by its database identifier. Results are cached
     * under the "items" cache by ID, evicted on item updates or deletions.
     *
     * @param id item ID
     * @return item response
     * @throws ItemNotFoundException if no item exists with the given id
     */
    @Override
    @Cacheable("items")
    public ItemResponse getById(Long id) {
        return ItemResponse.fromEntity(findItem(id));
    }

    /**
     * Retrieves a single item by its URL-friendly slug. Results are cached
     * under the "items" cache by slug, evicted on item updates or deletions.
     *
     * @param slug item slug
     * @return item response
     * @throws ItemNotFoundException if no item exists with the given slug
     */
    @Override
    @Cacheable("items")
    public ItemResponse getBySlug(String slug) {
        Item item = itemLookupService.findBySlug(slug)
                .orElseThrow(() -> ItemNotFoundException.slug(slug));
        return ItemResponse.fromEntity(item);
    }

    /**
     * Retrieves a paginated list of all items, sorted by newest first.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated item responses
     */
    @Override
    public PageResponse<ItemResponse> getAll(int page, int size) {
        var itemPage = itemLookupService.findAll(page, size);
        return PageResponse.of(itemPage, ItemResponse::fromEntity);
    }

    /**
     * Retrieves a paginated list of newly arrived published items.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated item responses
     */
    @Override
    public PageResponse<ItemResponse> getNewArrivals(int page, int size) {
        var itemPage = itemLookupService.findNewArrivalsPage(
                ProductPublishingStatus.PUBLISHED, page, size);
        return PageResponse.of(itemPage, ItemResponse::fromEntity);
    }

    /**
     * Retrieves a paginated list of items currently on sale (with active discount).
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated item responses
     */
    @Override
    public PageResponse<ItemResponse> getSaleItems(int page, int size) {
        var itemPage = itemLookupService.findOnSaleItemsPage(page, size);
        return PageResponse.of(itemPage, ItemResponse::fromEntity);
    }

    /**
     * Retrieves a paginated list of items belonging to the specified category.
     *
     * @param slug category slug
     * @param page zero-based page index
     * @param size page size
     * @return paginated item responses
     */
    @Override
    public PageResponse<ItemResponse> getByCategorySlug(String slug, int page, int size) {
        var itemPage = itemLookupService.findByCategorySlug(slug, page, size);
        return PageResponse.of(itemPage, ItemResponse::fromEntity);
    }

    /**
     * Retrieves the top-selling items based on order volume in the last 30 days.
     *
     * <p>Results are returned in ranking order as determined by total units sold.</p>
     *
     * @param size maximum number of top-selling items to return
     * @return ordered list of top-selling item responses
     */
    @Override
    public List<ItemResponse> getTopSelling(int size) {
        Instant since = clock.instant().minus(30, ChronoUnit.DAYS);
        var rows = itemLookupService.findTopSellingItemIds(since, size);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> itemIds = rows.stream()
                .map(r -> r.getItemId())
                .toList();
        List<Item> items = itemLookupService.findAllByIds(itemIds);
        Map<Long, Item> itemMap = items.stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));
        return itemIds.stream()
                .map(itemMap::get)
                .filter(Objects::nonNull)
                .map(ItemResponse::fromEntity)
                .toList();
    }

    /**
     * Generates a unique URL-friendly slug.
     *
     * <p>The initial slug is produced by {@link SlugUtil}. If another item
     * already uses the same slug, numeric suffixes are appended until a unique
     * slug is found.</p>
     *
     * <p>Example slug generation:</p>
     * <pre>
     * iphone-15-pro
     * iphone-15-pro-2
     * iphone-15-pro-3
     * </pre>
     *
     * @param itemName item name
     * @return unique slug
     */
    private String generateUniqueSlug(String itemName) {
        String baseSlug = SlugUtil.toSlug(itemName);
        String slug = baseSlug;
        int counter = 2;

        while (itemLookupService.existsBySlug(slug)) {
            if (counter > 1000) {
                throw SlugGenerationFailedException.forEntity("item", itemName, 1000);
            }
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }
}
