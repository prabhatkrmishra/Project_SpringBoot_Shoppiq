package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.request.ItemRequest;
import com.pkmprojects.shoppiq.dto.response.ItemResponse;
import com.pkmprojects.shoppiq.entity.Category;
import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.entity.ItemDetails;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.exception.CategoryNotFoundException;
import com.pkmprojects.shoppiq.exception.DuplicateItemException;
import com.pkmprojects.shoppiq.exception.ItemNotFoundException;
import com.pkmprojects.shoppiq.repository.CategoryRepository;
import com.pkmprojects.shoppiq.repository.ItemRepository;
import com.pkmprojects.shoppiq.service.ItemService;
import com.pkmprojects.shoppiq.util.SlugUtil;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link ItemService}.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    public ItemServiceImpl(
            ItemRepository itemRepository,
            CategoryRepository categoryRepository
    ) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
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
                itemRepository.save(item);
                return;
            } catch (DataIntegrityViolationException e) {
                if (e.getMessage() != null && e.getMessage().contains("slug")) {
                    item.setSlug(generateUniqueSlug(item.getName()));
                    attempts++;
                } else {
                    throw e;
                }
            }
        }
        throw new RuntimeException("Failed to generate unique slug after 10 attempts");
    }

    private Item findItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> ItemNotFoundException.id(id));
    }

    @Override
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

        Map<Long, Category> categories = categoryRepository
                .findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        if (categories.size() != categoryIds.size()) {
            throw CategoryNotFoundException.ids();
        }

        Set<String> existingSkus = itemRepository.findExistingSkus(requestSkus);
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

    @Override
    public ItemResponse getById(Long id) {
        return ItemResponse.fromEntity(findItem(id));
    }

    @Override
    public ItemResponse getBySlug(String slug) {
        Item item = itemRepository.findBySlug(slug)
                .orElseThrow(() -> ItemNotFoundException.slug(slug));
        return ItemResponse.fromEntity(item);
    }

    @Override
    public PageResponse<ItemResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var itemPage = itemRepository.findAllByOrderByNameAsc(pageable);
        return PageResponse.of(itemPage, ItemResponse::fromEntity);
    }

    @Override
    public PageResponse<ItemResponse> getNewArrivals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var itemPage = itemRepository.findNewArrivalsPage(
                ProductPublishingStatus.PUBLISHED, pageable);
        return PageResponse.of(itemPage, ItemResponse::fromEntity);
    }

    @Override
    public PageResponse<ItemResponse> getSaleItems(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var itemPage = itemRepository.findOnSaleItemsPage(pageable);
        return PageResponse.of(itemPage, ItemResponse::fromEntity);
    }

    @Override
    public PageResponse<ItemResponse> getByCategorySlug(String slug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var itemPage = itemRepository.findByCategorySlug(slug, pageable);
        return PageResponse.of(itemPage, ItemResponse::fromEntity);
    }

    @Override
    public List<ItemResponse> getTopSelling(int size) {
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Object[]> rows = itemRepository.findTopSellingItemIds(since, size);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> itemIds = rows.stream()
                .map(row -> ((Number) row[0]).longValue())
                .toList();
        List<Item> items = itemRepository.findAllById(itemIds);
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
     * <p>
     * The initial slug is produced by {@link SlugUtil}. If another item
     * already uses the same slug, numeric suffixes are appended until a
     * unique slug is found.
     * </p>
     *
     * <h4>Example</h4>
     *
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

        while (itemRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }
}