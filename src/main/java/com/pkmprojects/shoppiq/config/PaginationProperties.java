package com.pkmprojects.shoppiq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <strong>Spring Boot Concept:</strong> {@code @ConfigurationProperties}
 * record bound to {@code app.pagination.*} in {@code application.yaml}.
 *
 * <p>Provides externalized pagination defaults (default page size, max page
 * size, and role-specific sizes for admin, seller, and catalog endpoints).
 * Allows operators to tune page sizes without touching code.</p>
 *
 * @author prabhatkrmishra
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
