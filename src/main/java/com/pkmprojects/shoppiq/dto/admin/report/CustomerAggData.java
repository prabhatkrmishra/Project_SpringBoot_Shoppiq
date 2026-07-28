package com.pkmprojects.shoppiq.dto.admin.report;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <strong>Spring Boot Concept:</strong> Data transfer record for customer-level aggregated analytics.
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record CustomerAggData(Long userId, String username, String email, long orderCount,
                              BigDecimal totalSpent, BigDecimal avgOrder, LocalDate firstOrder,
                              LocalDate lastOrder) {
}
