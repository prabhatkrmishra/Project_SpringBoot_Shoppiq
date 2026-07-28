package com.pkmprojects.shoppiq.repository.item.projection;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA interface-based projection for ranking items by total
 * quantity sold in delivered orders since a given date.
 *
 * <p><strong>What this demonstrates:</strong></p>
 * <ul>
 *   <li><strong>Interface-based projection with native queries</strong> — Spring Data JPA
 *       matches native query column aliases (e.g., {@code i.id AS item_id}) to getter methods
 *       ({@link #getItemId()}), eliminating fragile {@code Object[]} index-based access.</li>
 *   <li><strong>Read-only DTO</strong> — Projections carry no managed persistence state,
 *       making them lightweight and safe for reporting use cases.</li>
 * </ul>
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
