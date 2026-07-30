package com.pkmprojects.shoppiq.repository.category;

import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.repository.category.projection.CategorySalesRanking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for the {@link Category} aggregate.
 *
 * <p>Provides methods to query categories by slug, name, and various filters for catalog management.
 * The repository supports paginated queries for category listing, search by name/description,
 * and aggregate queries for sales analytics and reporting.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by its URL-friendly slug.
     *
     * @param slug category slug
     * @return matching category, if present
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Finds a category by its name ignoring character case.
     *
     * @param name category name
     * @return matching category, if present
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Determines whether another category already exists with the supplied
     * name while excluding the specified identifier.
     *
     * <p>
     * Primarily used during update operations to ignore the entity currently
     * being modified.
     * </p>
     *
     * @param name category name
     * @param id   category identifier to exclude
     * @return {@code true} if another matching category exists
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    /**
     * Determines whether a category already exists with the supplied slug.
     *
     * @param slug category slug
     * @return {@code true} if found
     */
    boolean existsBySlug(String slug);

    List<Category> findAllByOrderByNameAsc();

    Page<Category> findAllByOrderByNameAsc(Pageable pageable);

    Page<Category> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable);

    @Query(value = """
            SELECT c.id, c.name, c.slug, c.description, SUM(oi.quantity) AS total_qty
            FROM categories c
            JOIN item_details idt ON idt.category_id = c.id
            JOIN order_items oi ON oi.item_details_id = idt.id
            JOIN orders o ON o.id = oi.order_id
            WHERE o.status = 'DELIVERED' AND o.placed_at >= :since
            GROUP BY c.id, c.name, c.slug, c.description
            ORDER BY total_qty DESC
            LIMIT :limit""",
            nativeQuery = true)
    List<CategorySalesRanking> findTopSellingCategoryIds(
            @Param("since") java.time.Instant since,
            @Param("limit") int limit
    );
}
