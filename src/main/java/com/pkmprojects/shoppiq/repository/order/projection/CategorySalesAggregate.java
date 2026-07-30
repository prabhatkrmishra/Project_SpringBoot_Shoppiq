package com.pkmprojects.shoppiq.repository.order.projection;

import java.math.BigDecimal;

/**
 * Projection for aggregating sales per category within a date range.
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface CategorySalesAggregate {

    /**
     * The category identifier.
     *
     * @return category ID
     */
    Long getCategoryId();

    /**
     * The category name.
     *
     * @return category name
     */
    String getCategoryName();

    /**
     * Total quantity sold for this category.
     *
     * @return quantity sold
     */
    Long getQuantitySold();

    /**
     * Total revenue for this category.
     *
     * @return revenue
     */
    BigDecimal getRevenue();

    /**
     * Number of unique products sold in this category.
     *
     * @return unique products sold count
     */
    Long getUniqueProductsSold();
}
