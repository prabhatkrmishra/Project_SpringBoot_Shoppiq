package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.item.ItemRequest;
import com.pkmprojects.shoppiq.dto.item.ItemResponse;
import com.pkmprojects.shoppiq.entity.user.User;

/**
 * Business contract for seller product lifecycle management.
 *
 * <p>Defines operations for CRUD with ownership verification, SKU uniqueness
 * enforcement, and DRAFT publishing status for new products.</p>
 *
 * @author prabhatkrmishra
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
     * Retrieves all products belonging to the authenticated seller, paginated.
     *
     * @param user the authenticated user
     * @param page page number (0-based)
     * @param size page size
     * @return paginated seller's products
     */
    PageResponse<ItemResponse> getMyProducts(User user, int page, int size);

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
