package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.dto.request.ItemRequest;
import com.pkmprojects.shoppiq.dto.response.ItemResponse;
import com.pkmprojects.shoppiq.entity.User;

import java.util.List;

/**
 * Business contract for seller product management.
 *
 * <p>
 * Handles the lifecycle of products owned by a seller. Sellers can
 * create, read, update and delete their own products. New products
 * start in {@code DRAFT} status and require admin publishing before
 * becoming visible to customers.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Create a new product as DRAFT.</li>
 *     <li>List all products belonging to the authenticated seller.</li>
 *     <li>Retrieve a specific product by ID (with ownership check).</li>
 *     <li>Update an existing product (with ownership check).</li>
 *     <li>Delete a product (with ownership check).</li>
 *     <li>Enforce seller-level preconditions (ACTIVE, not SUSPENDED).</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>The seller is derived from the authenticated user, not from
 *         the request payload.</li>
 *     <li>Ownership verification ensures sellers can only manage their
 *         own products.</li>
 *     <li>SKU uniqueness is enforced across all sellers.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface SellerProductService {

    /**
     * Creates a new product for the authenticated seller.
     *
     * <p>The product is created with {@code DRAFT} publishing status.
     * The seller must be in {@code ACTIVE} status and not suspended.</p>
     *
     * @param request the product creation details
     * @param user    the authenticated user
     * @return the created product
     */
    ItemResponse createProduct(ItemRequest request, User user);

    /**
     * Retrieves all products belonging to the authenticated seller.
     *
     * @param user the authenticated user
     * @return list of the seller's products
     */
    List<ItemResponse> getMyProducts(User user);

    /**
     * Retrieves a specific product belonging to the authenticated seller.
     *
     * @param id   the product identifier
     * @param user the authenticated user
     * @return the matching product
     */
    ItemResponse getMyProductById(Long id, User user);

    /**
     * Updates an existing product belonging to the authenticated seller.
     *
     * @param id      the product identifier
     * @param request the updated product details
     * @param user    the authenticated user
     * @return the updated product
     */
    ItemResponse updateProduct(Long id, ItemRequest request, User user);

    /**
     * Deletes a product belonging to the authenticated seller.
     *
     * @param id   the product identifier
     * @param user the authenticated user
     */
    void deleteProduct(Long id, User user);
}
