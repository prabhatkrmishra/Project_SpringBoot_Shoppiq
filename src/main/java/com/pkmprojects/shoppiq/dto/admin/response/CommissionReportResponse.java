package com.pkmprojects.shoppiq.dto.admin.response;

import java.math.BigDecimal;

/**
 * Admin-facing commission report DTO.
 *
 * <p>Shows commission details for each seller, including revenue
 * and computed commission based on the seller's commission rate.</p>
 *
 * @param sellerId       seller identifier
 * @param businessName   seller business name
 * @param totalOrders    total orders containing seller's products
 * @param totalRevenue   total revenue from seller's items in paid orders
 * @param commissionRate seller's commission rate (percentage)
 * @param commissionEarned computed commission amount
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record CommissionReportResponse(
        Long sellerId,
        String businessName,
        long totalOrders,
        BigDecimal totalRevenue,
        BigDecimal commissionRate,
        BigDecimal commissionEarned
) {
    public static CommissionReportResponse from(
            Long sellerId,
            String businessName,
            long totalOrders,
            BigDecimal totalRevenue,
            BigDecimal commissionRate
    ) {
        BigDecimal commissionEarned = totalRevenue.multiply(
                commissionRate.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP)
        ).setScale(2, java.math.RoundingMode.HALF_UP);

        return new CommissionReportResponse(
                sellerId, businessName, totalOrders, totalRevenue,
                commissionRate, commissionEarned
        );
    }
}
