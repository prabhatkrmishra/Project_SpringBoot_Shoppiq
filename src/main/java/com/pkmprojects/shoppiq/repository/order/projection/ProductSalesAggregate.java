package com.pkmprojects.shoppiq.repository.order.projection;

import java.math.BigDecimal;

/**
 * Projection for aggregating sales per product within a date range.
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface ProductSalesAggregate {

    /**
     * The item identifier.
     *
     * @return item ID
     */
    Long getItemId();

    /**
     * The item name.
     *
     * @return item name
     */
    String getItemName();

    /**
     * The SKU.
     *
     * @return SKU
     */
    String getSku();

    /**
     * Total quantity sold.
     *
     * @return quantity sold
     */
    Long getQuantitySold();

    /**
     * Total revenue.
     *
     * @return revenue
     */
    BigDecimal getRevenue();
}
