package com.pkmprojects.shoppiq.service.category;

import com.pkmprojects.shoppiq.dto.category.CategoryRequest;
import com.pkmprojects.shoppiq.dto.category.CategoryResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;

import java.util.List;

/**
 * Business contract for product category management.
 *
 * <p>Defines operations for creating, updating, deleting, and retrieving
 * categories with automatic slug generation and uniqueness enforcement.
 * This service acts as the primary boundary between the presentation layer
 * (controllers) and the persistence layer (repositories), following the
 * {@code CategoryController → CategoryService → CategoryLookupService /
 * CategoryWriteService} delegation pattern.</p>
 *
 * <p>Business rules enforced include: category names must be unique
 * (case-insensitive), category slugs must be unique, slugs are automatically
 * generated from category names via {@code SlugUtil}, and categories
 * referenced by products cannot be deleted. All mutations invalidate the
 * categories cache to ensure fresh data on subsequent reads.</p>
 *
 * <p>Write operations (create, update, delete) are transactional and may
 * throw {@code DuplicateCategoryException} for name conflicts or
 * {@code CategoryNotFoundException} for missing identifiers. Read
 * operations are read-only transactions with caching enabled.</p>
 *
 * @author prabhatkrmishra
 * @see CategoryLookupService
 * @see CategoryWriteService
 * @since 1.0.0
 */
public interface CategoryService {

    /**
     * Creates a new product category.
     *
     * <p>
     * The implementation performs all required validations before persisting
     * the category.
     * </p>
     *
     * <ul>
     *     <li>Validates request data.</li>
     *     <li>Checks duplicate category names.</li>
     *     <li>Generates a URL-friendly slug.</li>
     *     <li>Ensures slug uniqueness.</li>
     * </ul>
     *
     * @param request category creation request
     * @return newly created category
     */
    CategoryResponse create(CategoryRequest request);

    /**
     * Creates multiple categories at once.
     *
     * @param requests list of category creation requests
     * @return list of newly created categories
     */
    List<CategoryResponse> createBulk(List<CategoryRequest> requests);

    /**
     * Updates an existing category.
     *
     * <p>
     * If the category name changes, a new slug is generated automatically.
     * Uniqueness validation is performed before the update is persisted.
     * </p>
     *
     * @param id      identifier of the category to update
     * @param request updated category data
     * @return updated category
     */
    CategoryResponse update(Long id, CategoryRequest request);

    /**
     * Deletes a category.
     *
     * <p>
     * Implementations should prevent deletion when products still reference
     * the category.
     * </p>
     *
     * @param id identifier of the category
     */
    void delete(Long id);

    /**
     * Retrieves a category by its database identifier.
     *
     * @param id category identifier
     * @return matching category
     */
    CategoryResponse getById(Long id);

    /**
     * Retrieves a category using its URL slug.
     *
     * @param slug URL-friendly category slug
     * @return matching category
     */
    CategoryResponse getBySlug(String slug);

    /**
     * Retrieves all categories.
     *
     * <p>
     * The returned list is typically ordered alphabetically by category name.
     * </p>
     *
     * @return list of all categories
     */
    List<CategoryResponse> getAll();

    /**
     * Retrieves all categories, paginated.
     *
     * <p>
     * The returned page is ordered alphabetically by category name.
     * </p>
     *
     * @param page page number (0-based)
     * @param size page size
     * @return paginated category responses
     */
    PageResponse<CategoryResponse> getAll(int page, int size);

    /**
     * Retrieves categories, paginated and optionally filtered by name or description.
     *
     * @param page   page number (0-based)
     * @param size   page size
     * @param search optional search term to filter by name or description
     * @return paginated category responses
     */
    PageResponse<CategoryResponse> getAll(int page, int size, String search);

    /**
     * Retrieves the top-selling categories from the last 30 days of delivered orders.
     *
     * @param size number of categories to return
     * @return ordered list of top-selling categories
     */
    List<CategoryResponse> getTopSelling(int size);
}
