package com.pkmprojects.shoppiq.dto.admin.report;

import java.math.BigDecimal;

public record CategoryPerformanceData(Long categoryId, String categoryName, long quantitySold,
                                      BigDecimal revenue, long uniqueProductsSold) {
}
