package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.request.ItemRequest;
import com.pkmprojects.shoppiq.dto.response.ItemResponse;

import java.util.List;

/**
 * Business contract for managing catalog items.
 *
 * <p>
 * Defines the operations available for creating, retrieving, updating
 * and deleting products within the Shoppiq catalog.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Create new products.</li>
 *     <li>Retrieve existing products.</li>
 *     <li>Update product information.</li>
 *     <li>Delete products.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Works exclusively with DTOs.</li>
 *     <li>Does not expose persistence entities.</li>
 *     <li>Implemented by {@code ItemServiceImpl}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface ItemService {

    /**
     * Creates a new catalog item.
     *
     * @param request product information
     * @return created product
     */
    ItemResponse create(ItemRequest request);

    /**
     * Creates list of catalog item.
     *
     * @param request product information list
     * @return created product
     */
    List<ItemResponse> createBulk(List<ItemRequest> request);

    /**
     * Retrieves a product by its identifier.
     *
     * @param id product identifier
     * @return matching product
     */
    ItemResponse getById(Long id);

    /**
     * Retrieves every product in the catalog.
     *
     * @return ordered product list
     */
    List<ItemResponse> getAll();

    /**
     * Updates an existing product.
     *
     * @param id      product identifier
     * @param request updated product information
     * @return updated product
     */
    ItemResponse update(Long id, ItemRequest request);

    /**
     * Deletes a product.
     *
     * @param id product identifier
     */
    void delete(Long id);
}