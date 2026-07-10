package com.pkmprojects.shoppiq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized pagination defaults.
 *
 * <p>
 * Bound to {@code app.pagination.*} in {@code application.yaml}.
 * Allows operators to tune page sizes without touching code.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "app.pagination")
public record PaginationProperties(
        int defaultPageSize,
        int maxPageSize,
        int adminPageSize,
        int sellerPageSize,
        int catalogPageSize
) {
}
