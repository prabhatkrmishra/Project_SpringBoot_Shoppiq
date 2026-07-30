package com.pkmprojects.shoppiq.repository.item.projection;

/**
 * Projection for ranking items by total quantity sold in delivered orders.
 *
 * @author prabhatkrmishra
 * @since 1.5.0
 */
public interface ItemSalesRanking {

    /**
     * The item identifier.
     *
     * @return item ID
     */
    Long getItemId();
}
