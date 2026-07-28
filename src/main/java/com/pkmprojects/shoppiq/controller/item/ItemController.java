package com.pkmprojects.shoppiq.controller.item;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.item.ItemResponse;
import com.pkmprojects.shoppiq.service.item.ItemService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> REST controller responsible for managing catalog items.
 *
 * <p>
 * Exposes public endpoints for browsing the product catalog.
 * Product creation, update and deletion are handled by seller
 * and admin controllers respectively.
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;
    private final PaginationProperties pagination;

    public ItemController(ItemService itemService, PaginationProperties pagination) {
        this.itemService = itemService;
        this.pagination = pagination;
    }

    /**
     * Returns a paginated list of all published items.
     *
     * @param page zero-based page index
     * @param size page size (capped by {@code pagination.maxPageSize()})
     * @return 200 OK with page of item responses
     */
    @GetMapping("/all")
    public PageResponse<ItemResponse> getAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return itemService.getAll(page, size);
    }

    /**
     * Returns a single item by its ID.
     *
     * @param id the item ID (must be positive)
     * @return 200 OK with the item response
     */
    @GetMapping("/{id}")
    public ItemResponse getById(
            @PathVariable @Positive(message = "Item id must be a positive number") Long id
    ) {
        return itemService.getById(id);
    }

    /**
     * Returns a single item by its URL slug.
     *
     * @param slug the item slug
     * @return 200 OK with the item response
     */
    @GetMapping("/slug/{slug}")
    public ItemResponse getBySlug(@PathVariable String slug) {
        return itemService.getBySlug(slug);
    }

    /**
     * Returns a paginated list of new-arrival items, sorted by creation date descending.
     *
     * @param page zero-based page index
     * @param size page size (capped by {@code pagination.maxPageSize()})
     * @return 200 OK with page of item responses
     */
    @GetMapping("/new-arrivals")
    public PageResponse<ItemResponse> getNewArrivals(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return itemService.getNewArrivals(page, size);
    }

    /**
     * Returns a paginated list of items currently on sale.
     *
     * @param page zero-based page index
     * @param size page size (capped by {@code pagination.maxPageSize()})
     * @return 200 OK with page of item responses
     */
    @GetMapping("/sale")
    public PageResponse<ItemResponse> getSaleItems(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return itemService.getSaleItems(page, size);
    }

    /**
     * Returns a paginated list of items in a specific category.
     *
     * @param slug the category slug
     * @param page zero-based page index
     * @param size page size (capped by {@code pagination.maxPageSize()})
     * @return 200 OK with page of item responses
     */
    @GetMapping("/category/{slug}")
    public PageResponse<ItemResponse> getByCategorySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return itemService.getByCategorySlug(slug, page, size);
    }

    /**
     * Returns the top-selling items across the store.
     *
     * @param size maximum number of items to return (capped by {@code pagination.maxPageSize()})
     * @return 200 OK with list of top-selling item responses
     */
    @GetMapping("/top-selling")
    public List<ItemResponse> getTopSelling(
            @RequestParam(defaultValue = "8") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return itemService.getTopSelling(size);
    }
}
