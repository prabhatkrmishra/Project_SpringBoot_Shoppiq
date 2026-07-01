package com.pkmprojects.shoppiq.dto.admin.analytics;

import java.math.BigDecimal;

/**
 * Top selling product data point for analytics.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record TopSellingProductData(
        Long itemId,
        String itemName,
        String sku,
        Long totalQuantitySold,
        BigDecimal totalRevenue
) {
}
