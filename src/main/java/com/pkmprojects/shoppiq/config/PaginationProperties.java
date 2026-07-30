package com.pkmprojects.shoppiq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for pagination defaults across the application.
 *
 * <p>This record binds to the {@code app.pagination.*} prefix in
 * {@code application.yaml} and provides role-specific page size defaults.
 * Different user roles have different pagination needs: admins browsing
 * large dashboards may prefer larger page sizes, while catalog browsing
 * by anonymous customers benefits from smaller, faster-loading pages.</p>
 *
 * <p>The properties are consumed by service and controller layers to apply
 * default page sizes when the client does not specify a size parameter.
 * The {@link #maxPageSize} acts as an upper bound to prevent clients from
 * requesting excessively large result sets that could cause memory or
 * performance issues.</p>
 *
 * @param defaultPageSize the fallback page size when no role-specific default applies
 * @param maxPageSize     the absolute maximum page size any client may request
 * @param adminPageSize   the default page size for admin dashboard endpoints
 * @param sellerPageSize  the default page size for seller management endpoints
 * @param catalogPageSize the default page size for public catalog browsing
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
