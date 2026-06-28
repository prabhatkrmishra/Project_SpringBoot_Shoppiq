package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.dto.request.ItemRequest;
import com.pkmprojects.shoppiq.dto.response.ItemResponse;
import com.pkmprojects.shoppiq.service.ItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for managing catalog items.
 *
 * <p>
 * Exposes endpoints for creating, retrieving, updating and deleting
 * products available in the Shoppiq catalog.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Accept incoming HTTP requests.</li>
 *     <li>Validate request payloads.</li>
 *     <li>Delegate business logic to {@link ItemService}.</li>
 *     <li>Return DTOs as API responses.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Contains no business logic.</li>
 *     <li>Contains no persistence logic.</li>
 *     <li>Acts only as the HTTP boundary.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/items")
public class ItemController {

    /**
     * Item service.
     */
    private final ItemService itemService;

    /**
     * Creates a controller instance.
     *
     * @param itemService item service
     */
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Creates a new catalog item.
     *
     * @param request item creation request
     * @return created item
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse create(
            @Valid @RequestBody ItemRequest request
    ) {
        return itemService.create(request);
    }

    /**
     * Creates a list of catalog items in bulk.
     *
     * @param request list of item creation requests
     * @return list of created items
     */
    @PostMapping("/create/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ItemResponse> create(
            @Valid @RequestBody List<ItemRequest> request
    ) {
        return itemService.createBulk(request);
    }

    /**
     * Retrieves an item by its identifier.
     *
     * @param id item identifier — must be a positive number
     * @return matching item
     */
    @GetMapping("/{id}")
    public ItemResponse getById(
            @PathVariable
            @Positive(message = "Item id must be a positive number")
            Long id
    ) {
        return itemService.getById(id);
    }

    /**
     * Retrieves every catalog item.
     *
     * @return ordered item list
     */
    @GetMapping("/all")
    public List<ItemResponse> getAll() {
        return itemService.getAll();
    }

    /**
     * Updates an existing catalog item.
     *
     * @param id      item identifier — must be a positive number
     * @param request updated item information
     * @return updated item
     */
    @PutMapping("/update/{id}")
    public ItemResponse update(
            @PathVariable
            @Positive(message = "Item id must be a positive number")
            Long id,
            @Valid @RequestBody ItemRequest request
    ) {
        return itemService.update(id, request);
    }

    /**
     * Deletes an existing catalog item.
     *
     * @param id item identifier — must be a positive number
     */
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable
            @Positive(message = "Item id must be a positive number")
            Long id
    ) {
        itemService.delete(id);
    }
}
