package com.pkmprojects.shoppiq.dto.admin.analytics;

import java.math.BigDecimal;

/**
 * Top category data point for analytics.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record TopCategoryData(
        Long categoryId,
        String categoryName,
        Long totalQuantitySold,
        BigDecimal totalRevenue
) {
}
