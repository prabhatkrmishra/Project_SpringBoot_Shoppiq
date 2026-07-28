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
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link Category} persistence operations.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Derived exists queries</strong> — {@code existsByNameIgnoreCase} translates to
 *       {@code SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM categories WHERE LOWER(name) = LOWER(?)}.</li>
 *   <li><strong>Negated property path</strong> — {@code existsByNameIgnoreCaseAndIdNot} shows
 *       {@code IdNot} maps to {@code id <> ?}, useful for "unique except myself" checks during updates.</li>
 *   <li><strong>Pagination</strong> — Accepting {@link org.springframework.data.domain.Pageable}
 *       parameters enables automatic {@code LIMIT} / {@code OFFSET} and count queries.</li>
 *   <li><strong>{@code ContainingIgnoreCase}</strong> — {@code findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase}
 *       generates {@code WHERE LOWER(name) LIKE LOWER(CONCAT('%', ?, '%')) OR LOWER(description) LIKE ...}.</li>
 *   <li><strong>Native queries</strong> — {@code findTopSellingCategoryIds} uses a raw SQL
 *       query with joins, aggregation, {@code GROUP BY}, {@code ORDER BY}, and {@code LIMIT}
 *       to compute sales rankings.</li>
 *   <li><strong>Interface-based projections</strong> — The native query maps results to
 *       {@link com.pkmprojects.shoppiq.repository.category.projection.CategorySalesRanking}
 *       via column alias matching.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findBySlug(String)
 *       → SELECT * FROM categories WHERE slug = ?
 *   existsByNameIgnoreCase(String)
 *       → SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM categories WHERE LOWER(name) = LOWER(?)
 *   existsByNameIgnoreCaseAndIdNot(String, Long)
 *       → ...WHERE LOWER(name) = LOWER(?) AND id &lt;&gt; ?
 *   findAllByOrderByNameAsc
 *       → SELECT * FROM categories ORDER BY name ASC
 *   findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase
 *       → ...WHERE LOWER(name) LIKE LOWER(CONCAT('%', ?, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', ?, '%'))
 * </pre>
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

    @Query(value = "SELECT c.id, c.name, c.slug, c.description, SUM(oi.quantity) AS total_qty " +
            "FROM categories c " +
            "JOIN item_details idt ON idt.category_id = c.id " +
            "JOIN order_items oi ON oi.item_details_id = idt.id " +
            "JOIN orders o ON o.id = oi.order_id " +
            "WHERE o.status = 'DELIVERED' AND o.placed_at >= :since " +
            "GROUP BY c.id, c.name, c.slug, c.description " +
            "ORDER BY total_qty DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<CategorySalesRanking> findTopSellingCategoryIds(
            @Param("since") java.time.Instant since,
            @Param("limit") int limit
    );
}
