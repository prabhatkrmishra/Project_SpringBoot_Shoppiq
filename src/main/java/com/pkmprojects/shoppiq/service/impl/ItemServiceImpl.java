package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.request.ItemRequest;
import com.pkmprojects.shoppiq.dto.response.ItemResponse;
import com.pkmprojects.shoppiq.entity.Category;
import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.entity.ItemDetails;
import com.pkmprojects.shoppiq.exception.CategoryNotFoundException;
import com.pkmprojects.shoppiq.exception.DuplicateItemException;
import com.pkmprojects.shoppiq.exception.ItemNotFoundException;
import com.pkmprojects.shoppiq.repository.CategoryRepository;
import com.pkmprojects.shoppiq.repository.ItemRepository;
import com.pkmprojects.shoppiq.service.ItemService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link ItemService}.
 *
 * <p>
 * Responsible for managing catalog items and coordinating persistence,
 * validation and mapping between DTOs and entities.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Create catalog items.</li>
 *     <li>Retrieve catalog items.</li>
 *     <li>Update existing catalog items.</li>
 *     <li>Delete catalog items.</li>
 *     <li>Validate SKU uniqueness.</li>
 *     <li>Resolve category relationships.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Uses constructor injection.</li>
 *     <li>Works exclusively with DTOs.</li>
 *     <li>Delegates persistence to Spring Data repositories.</li>
 *     <li>All write operations execute inside transactions.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    /**
     * Item repository.
     */
    private final ItemRepository itemRepository;

    /**
     * Category repository.
     */
    private final CategoryRepository categoryRepository;

    /**
     * Creates a service instance.
     *
     * @param itemRepository     item repository
     * @param categoryRepository category repository
     */
    public ItemServiceImpl(
            ItemRepository itemRepository,
            CategoryRepository categoryRepository
    ) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Creates an {@link Item} entity from the supplied request.
     *
     * <p>
     * Used by both single and bulk creation operations.
     * The category is supplied by the caller to avoid repeated
     * database lookups during bulk inserts.
     * </p>
     *
     * @param request  request DTO
     * @param category resolved category
     * @return populated item entity
     */
    private Item buildItem(
            ItemRequest request,
            Category category
    ) {

        ItemDetails itemDetails = ItemDetails.builder()
                .brand(request.brand())
                .sku(request.sku())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .discountPercentage(request.discountPercentage())
                .category(category)
                .build();

        Item item = Item.builder()
                .name(request.name())
                .description(request.description())
                .itemDetails(itemDetails)
                .build();

        itemDetails.setItem(item);

        return item;
    }

    /**
     * Retrieves a category or throws an exception.
     *
     * @param id category identifier
     * @return category
     */
    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        CategoryNotFoundException.id(id)
                );
    }

    /**
     * Retrieves an item or throws an exception.
     *
     * @param id item identifier
     * @return item entity
     */
    private Item findItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() ->
                        ItemNotFoundException.id(id)
                );
    }

    /**
     * Validates SKU uniqueness during item creation.
     *
     * @param sku SKU
     */
    private void validateSku(String sku) {
        if (itemRepository.existsByItemDetailsSku(sku)) {
            throw DuplicateItemException.sku(sku);
        }
    }

    /**
     * Validates SKU uniqueness during updates.
     *
     * @param sku SKU
     * @param id  current item id
     */
    private void validateSku(String sku, Long id) {
        if (itemRepository.existsByItemDetailsSkuAndIdNot(sku, id)) {
            throw DuplicateItemException.sku(sku);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemResponse create(ItemRequest request) {
        validateSku(request.sku());

        Category category = findCategory(request.categoryId());

        Item item = buildItem(request, category);

        return ItemResponse.fromEntity(
                itemRepository.save(item)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ItemResponse> createBulk(List<ItemRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        /*
         * Detect duplicate SKUs within the request payload.
         */
        Set<String> requestSkus = new HashSet<>();

        for (ItemRequest request : requests) {
            if (!requestSkus.add(request.sku())) {
                throw DuplicateItemException.sku(request.sku());
            }
        }

        /*
         * Resolve all categories using a single database query.
         */
        Set<Long> categoryIds = requests.stream()
                .map(ItemRequest::categoryId)
                .collect(Collectors.toSet());

        Map<Long, Category> categories = categoryRepository
                .findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        Function.identity()
                ));

        /*
         * Ensure every requested category exists.
         */
        if (categories.size() != categoryIds.size()) {
            throw CategoryNotFoundException.ids();
        }

        /*
         * Check whether any SKU already exists in the database.
         */
        Set<String> existingSkus = itemRepository.findExistingSkus(requestSkus);
        if (!existingSkus.isEmpty()) {
            throw DuplicateItemException.sku(
                    existingSkus.iterator().next()
            );
        }

        /*
         * Build entities.
         */
        List<Item> items = requests.stream()
                .map(request ->
                        buildItem(
                                request,
                                categories.get(request.categoryId())
                        )
                )
                .toList();

        /*
         * Persist all items in a single batch.
         */
        return itemRepository.saveAll(items)
                .stream()
                .map(ItemResponse::fromEntity)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemResponse getById(Long id) {
        return ItemResponse.fromEntity(
                findItem(id)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ItemResponse> getAll() {
        return itemRepository.findAllByOrderByNameAsc()
                .stream()
                .map(ItemResponse::fromEntity)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemResponse update(
            Long id,
            ItemRequest request
    ) {
        validateSku(request.sku(), id);

        Item item = findItem(id);
        ItemDetails details = item.getItemDetails();
        Category category = findCategory(request.categoryId());

        item.setName(request.name());
        item.setDescription(request.description());

        details.setBrand(request.brand());
        details.setSku(request.sku());
        details.setPrice(request.price());
        details.setStockQuantity(request.stockQuantity());
        details.setDiscountPercentage(request.discountPercentage());
        details.setCategory(category);

        return ItemResponse.fromEntity(
                itemRepository.save(item)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Long id) {
        Item item = findItem(id);
        itemRepository.delete(item);
    }
}