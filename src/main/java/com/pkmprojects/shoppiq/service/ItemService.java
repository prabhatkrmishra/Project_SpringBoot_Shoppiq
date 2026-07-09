package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.request.ItemRequest;
import com.pkmprojects.shoppiq.dto.response.ItemResponse;

import java.util.List;

/**
 * Business contract for managing catalog items.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface ItemService {

    /**
     * Creates a list of catalog items (bulk import).
     *
     * @param request product information list
     * @return created products
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
     * Retrieves a product by its slug.
     *
     * @param slug URL-friendly identifier
     * @return matching product
     */
    ItemResponse getBySlug(String slug);

    /**
     * Retrieves every product in the catalog.
     *
     * @return ordered product list
     */
    List<ItemResponse> getAll();
}