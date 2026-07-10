package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.response.ItemResponse;
import com.pkmprojects.shoppiq.service.ItemService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller responsible for managing catalog items.
 *
 * <p>
 * Exposes public endpoints for browsing the product catalog.
 * Product creation, update and deletion are handled by seller
 * and admin controllers respectively.
 * </p>
 *
 * @author PrabhatKrMishra
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

    @GetMapping("/all")
    public PageResponse<ItemResponse> getAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return itemService.getAll(page, size);
    }

    @GetMapping("/{id}")
    public ItemResponse getById(
            @PathVariable @Positive(message = "Item id must be a positive number") Long id
    ) {
        return itemService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    public ItemResponse getBySlug(@PathVariable String slug) {
        return itemService.getBySlug(slug);
    }

    @GetMapping("/new-arrivals")
    public PageResponse<ItemResponse> getNewArrivals(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return itemService.getNewArrivals(page, size);
    }

    @GetMapping("/sale")
    public PageResponse<ItemResponse> getSaleItems(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return itemService.getSaleItems(page, size);
    }

    @GetMapping("/category/{slug}")
    public PageResponse<ItemResponse> getByCategorySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return itemService.getByCategorySlug(slug, page, size);
    }
}
