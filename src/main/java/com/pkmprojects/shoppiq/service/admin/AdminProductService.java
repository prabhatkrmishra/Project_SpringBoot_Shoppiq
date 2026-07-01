package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminProductResponse;

import java.util.List;

/**
 * Business contract for admin product lifecycle management.
 *
 * <p>
 * Provides operations for reviewing and publishing seller products.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>List products pending admin review (DRAFT status).</li>
 *     <li>Publish a product (DRAFT → PUBLISHED).</li>
 *     <li>Reject a product (DRAFT → REJECTED).</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface AdminProductService {

    /**
     * Retrieves all products with DRAFT publishing status.
     *
     * @return list of pending products
     */
    List<AdminProductResponse> getPendingProducts();

    /**
     * Publishes a product by setting its status to PUBLISHED.
     *
     * @param itemId the product identifier
     * @return updated product response
     */
    AdminProductResponse publishProduct(Long itemId);

    /**
     * Rejects a product by setting its status to REJECTED.
     *
     * @param itemId the product identifier
     * @return updated product response
     */
    AdminProductResponse rejectProduct(Long itemId);
}
