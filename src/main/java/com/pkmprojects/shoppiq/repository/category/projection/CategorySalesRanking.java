package com.pkmprojects.shoppiq.repository.category.projection;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA interface-based projection for ranking categories by total
 * quantity sold in delivered orders since a given date.
 *
 * <p><strong>What this demonstrates:</strong></p>
 * <ul>
 *   <li><strong>Interface-based projection</strong> — Spring Data JPA maps native query
 *       result columns to this interface's getter methods using column alias matching
 *       (e.g., {@code c.name} → {@link #getName()}).</li>
 *   <li><strong>Compile-time safety</strong> — Unlike {@code Object[]} index-based access,
 *       projections provide type-safe access and IDE autocompletion.</li>
 *   <li><strong>Read-only</strong> — Projections carry no data, no JPA managed state, and
 *       cannot be used for writes, making them ideal for reporting.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.5.0
 */
public interface CategorySalesRanking {

    /**
     * The category identifier.
     *
     * @return category ID
     */
    Long getId();

    /**
     * The category name.
     *
     * @return category name
     */
    String getName();

    /**
     * The category URL slug.
     *
     * @return URL slug
     */
    String getSlug();

    /**
     * The category description.
     *
     * @return description text
     */
    String getDescription();
}
