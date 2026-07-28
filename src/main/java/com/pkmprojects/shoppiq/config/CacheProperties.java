package com.pkmprojects.shoppiq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * <strong>Spring Boot Concept:</strong> {@code @ConfigurationProperties}
 * record bound to {@code app.cache.*} in {@code application.yaml}.
 *
 * <p>Provides externalized per-cache TTL (time-to-live) values in milliseconds.
 * Each entry in the {@code ttl} map corresponds to a cache name used in
 * {@code @Cacheable} annotations (e.g. {@code banners}, {@code categories},
 * {@code items}).</p>
 *
 * <p>A future migration to Redis can consume these values via
 * {@code RedisCacheManagerBuilderCustomizer} or similar.</p>
 *
 * @param ttl map of cache name → TTL in milliseconds
 * @author prabhatkrmishra
 * @since 1.4.0
 */
@ConfigurationProperties(prefix = "app.cache")
public record CacheProperties(
        Map<String, Long> ttl
) {
}
