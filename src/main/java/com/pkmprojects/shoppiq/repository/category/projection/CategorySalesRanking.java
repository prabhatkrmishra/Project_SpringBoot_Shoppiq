package com.pkmprojects.shoppiq.repository.category.projection;

/**
 * Projection for ranking categories by total quantity sold in delivered orders.
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
