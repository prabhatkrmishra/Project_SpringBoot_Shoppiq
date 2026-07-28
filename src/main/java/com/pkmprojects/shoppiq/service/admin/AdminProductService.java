package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminProductResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;

/**
 * <strong>Spring Boot Concept:</strong> Business contract for admin product lifecycle management.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * This interface defines the <strong>Service layer</strong> contract for product lifecycle
 * operations. Architecture: {@code AdminProductController → AdminProductService → ItemLookupService / ItemWriteService}.
 * The interface decouples the controller from the implementation, enabling loose coupling and testability.
 * </p>
 *
 * <h2>Business Logic Responsibilities</h2>
 * <ul>
 *     <li>List products pending admin review (DRAFT publishing status).</li>
 *     <li>Approve/publish a product (DRAFT → PUBLISHED status transition).</li>
 *     <li>Reject a product (DRAFT → REJECTED status transition).</li>
 * </ul>
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
