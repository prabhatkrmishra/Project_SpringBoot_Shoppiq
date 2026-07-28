package com.pkmprojects.shoppiq.dto.admin.report;

import java.math.BigDecimal;

/**
 * <strong>Spring Boot Concept:</strong> Data transfer record for category-level sales performance metrics.
 *
 * <p>Provides aggregated sales data for a single product category,
 * including total quantity sold, total revenue, and the number of
 * unique products sold within that category.</p>
 *
 * <p><b>Why a record?</b> Java records are the preferred choice for
 * <i>reporting/projection DTOs</i> because they are immutable, require
 * zero boilerplate, and their canonical constructor works directly with
 * JPQL {@code SELECT NEW} expressions in the repository layer.</p>
 *
 * @param categoryId       the unique identifier of the category
 * @param categoryName     the display name of the category
 * @param quantitySold     total number of units sold in this category
 * @param revenue          total revenue generated from this category
 * @param uniqueProductsSold number of distinct products sold within the category
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record CategoryPerformanceData(Long categoryId, String categoryName, long quantitySold,
                                      BigDecimal revenue, long uniqueProductsSold) {
}
