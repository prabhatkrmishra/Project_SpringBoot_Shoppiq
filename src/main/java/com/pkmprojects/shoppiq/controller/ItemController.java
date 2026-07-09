package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.dto.response.ItemResponse;
import com.pkmprojects.shoppiq.service.ItemService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/all")
    public List<ItemResponse> getAll() {
        return itemService.getAll();
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
}
