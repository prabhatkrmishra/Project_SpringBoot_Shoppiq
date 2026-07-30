package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminProductResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;

/**
 * Business contract for admin product lifecycle management.
 *
 * <p>Defines operations for reviewing seller-submitted products and
 * transitioning publishing status between DRAFT, PUBLISHED, and REJECTED.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface AdminProductService {

    /**
     * Retrieves all products with DRAFT publishing status, paginated.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return paginated pending products
     */
    PageResponse<AdminProductResponse> getPendingProducts(int page, int size);

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
