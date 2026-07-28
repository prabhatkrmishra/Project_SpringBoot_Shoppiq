package com.pkmprojects.shoppiq.dto.admin.analytics;

import java.math.BigDecimal;

/**
 * Top selling product data point for analytics.
 *
 * <p>This <b>Java record</b> represents a top-selling product with its
 * SKU, total quantity sold, and revenue. Used in the admin dashboard to
 * display "best sellers" rankings.</p>
 *
 * <p><b>Pattern:</b> Like other analytics records, this is a
 * <i>projection DTO</i> — it only carries data read from the database
 * and never accepts user input, so no validation annotations are needed.</p>
 *
 * @author prabhatkrmishra
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
