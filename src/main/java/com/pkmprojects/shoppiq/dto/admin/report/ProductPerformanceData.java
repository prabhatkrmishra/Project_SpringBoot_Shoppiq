package com.pkmprojects.shoppiq.dto.admin.report;

import java.math.BigDecimal;

public record ProductPerformanceData(Long itemId, String itemName, String sku, long quantitySold,
                                     BigDecimal revenue, BigDecimal averagePrice, int currentStock) {
}
