package com.pkmprojects.shoppiq.service.category;

import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.repository.category.projection.CategorySalesRanking;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Read-only category query facade for lookup, search, and aggregate queries.
 *
 * <p>Decouples service-layer code from {@code CategoryRepository},
 * providing find, exists, count, search, and top-selling queries.</p>
 *
 * @author prabhatkrmishra
 * @see CategoryWriteService
 * @since 1.4.0
 */
public interface CategoryLookupService {

    /**
     * Finds a category by primary key.
     *
     * @param categoryId the category identifier
     * @return matching category, or empty if not found
     */
    Optional<Category> findById(Long categoryId);

    /**
     * Finds a category by its URL slug.
     *
     * @param slug URL-friendly identifier
     * @return matching category, or empty if not found
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Checks whether a category with the given name exists (case-insensitive).
     *
     * @param name category name to check
     * @return {@code true} if a matching category exists
     */
    boolean existsByName(String name);

    /**
     * Checks whether a category with the given name exists, excluding the specified ID.
     *
     * <p>Used during update operations to allow keeping the same name.</p>
     *
     * @param name category name to check
     * @param id   category ID to exclude
     * @return {@code true} if another category with this name exists
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Checks whether a category with the given slug exists.
     *
     * @param slug URL-friendly identifier
     * @return {@code true} if a matching category exists
     */
    boolean existsBySlug(String slug);

    /**
     * Returns the total number of categories.
     *
     * @return category count
     */
    long count();

    /**
     * Returns all categories ordered by name ascending.
     *
     * @return all categories
     */
    List<Category> findAll();

    /**
     * Returns a page of categories ordered by name ascending.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated categories
     */
    Page<Category> findAll(int page, int size);

    /**
     * Searches categories by name or description (case-insensitive contains).
     *
     * @param search text to match against name or description
     * @param page   zero-based page index
     * @param size   page size
     * @return matching categories, ordered by name ascending
     */
    Page<Category> findByNameOrDescriptionContaining(String search, int page, int size);

    /**
     * Returns categories ranked by total quantity sold in delivered orders
     * since the given date.
     *
     * @param since cutoff timestamp (inclusive)
     * @param limit max number of results
     * @return typed projections with category details
     */
    List<CategorySalesRanking> findTopSellingCategoryIds(Instant since, int size);

    /**
     * Returns all categories matching the given IDs.
     *
     * @param ids list of category identifiers
     * @return matching categories
     */
    List<Category> findAllByIds(List<Long> ids);
}
