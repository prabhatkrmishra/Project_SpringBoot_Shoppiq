package com.pkmprojects.shoppiq.service.item;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.item.ItemRequest;
import com.pkmprojects.shoppiq.dto.item.ItemResponse;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Business contract for managing catalog items.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * Defines the <strong>Service layer</strong> contract for the public product catalog.
 * Architecture: {@code ItemController → ItemService → ItemLookupService / ItemWriteService}.
 * This service provides the public-facing product queries used by the storefront.
 * </p>
 *
 * <h2>Business Logic Responsibilities</h2>
 * <ul>
 *   <li>Bulk import products with SKU deduplication and slug conflict resolution.</li>
 *   <li>Retrieve products by ID or slug.</li>
 *   <li>Paginated catalog listing with sorting.</li>
 *   <li>New arrivals (newest products first).</li>
 *   <li>On-sale items (products with active discounts).</li>
 *   <li>Products by category (via category slug).</li>
 *   <li>Top-selling products from the last 30 days.</li>
 * </ul>
 *
 * @author prabhatkrmishra
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
     * Retrieves every product in the catalog, paginated.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return paginated product list
     */
    PageResponse<ItemResponse> getAll(int page, int size);

    /**
     * Retrieves the latest products ordered by creation date, paginated.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return paginated newest products
     */
    PageResponse<ItemResponse> getNewArrivals(int page, int size);

    /**
     * Retrieves all products currently marked as on sale, paginated.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return paginated on-sale products
     */
    PageResponse<ItemResponse> getSaleItems(int page, int size);

    /**
     * Retrieves all products in a given category, paginated.
     *
     * @param slug category URL slug
     * @param page page number (0-based)
     * @param size page size
     * @return paginated products belonging to the category
     */
    PageResponse<ItemResponse> getByCategorySlug(String slug, int page, int size);

    /**
     * Retrieves the top-selling products from the last 30 days of delivered orders.
     *
     * @param size number of products to return
     * @return ordered list of top-selling products
     */
    List<ItemResponse> getTopSelling(int size);
}
