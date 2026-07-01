package com.pkmprojects.shoppiq.dto.admin.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CustomerAggData(Long userId, String username, String email, long orderCount,
                              BigDecimal totalSpent, BigDecimal avgOrder, LocalDate firstOrder,
                              LocalDate lastOrder) {
}
