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
 * REST controller for public catalog item browsing.
 *
 * <p>Exposes read-only endpoints for browsing the product catalog. Supports
 * browsing all published items, fetching by ID or slug, filtering by category,
 * viewing new arrivals and sale items, and retrieving top-selling products.
 * Product creation, update, and deletion are handled by seller and admin
 * controllers respectively.</p>
 *
 * <p>This controller acts as the HTTP boundary for catalog browsing. It delegates
 * all business logic — query filtering, pagination, slug resolution, and
 * category-based filtering — to {@link ItemService}. The controller handles
 * no business logic beyond page-size capping.</p>
 *
 * <p>No authentication is required. All endpoints are mounted under /items.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * GET    /items/all                — paginated list of all published items
 * GET    /items/{id}               — get a single item by ID
 * GET    /items/slug/{slug}        — get a single item by URL slug
 * GET    /items/new-arrivals       — paginated new arrival items
 * GET    /items/sale               — paginated sale items
 * GET    /items/category/{slug}    — paginated items by category slug
 * GET    /items/top-selling        — top-selling items across the store
 * </pre>
 *
 * @author prabhatkrmishra
 * @see ItemService
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
     * @param size page size (capped by the configured maximum)
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
     * <p>Slugs are URL-friendly identifiers used for SEO-friendly
     * product URLs.</p>
     *
     * @param slug the item URL slug
     * @return 200 OK with the item response
     */
    @GetMapping("/slug/{slug}")
    public ItemResponse getBySlug(@PathVariable String slug) {
        return itemService.getBySlug(slug);
    }

    /**
     * Returns a paginated list of new-arrival items, sorted by creation
     * date descending.
     *
     * @param page zero-based page index
     * @param size page size (capped by the configured maximum)
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
     * @param size page size (capped by the configured maximum)
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
     * @param slug the category slug to filter by
     * @param page zero-based page index
     * @param size page size (capped by the configured maximum)
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
     * @param size maximum number of items to return (capped by the configured maximum)
     * @return 200 OK with list of top-selling item responses
     */
    @GetMapping("/top-selling")
    public List<ItemResponse> getTopSelling(
            @RequestParam(defaultValue = "8") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return itemService.getTopSelling(size);
    }
}
