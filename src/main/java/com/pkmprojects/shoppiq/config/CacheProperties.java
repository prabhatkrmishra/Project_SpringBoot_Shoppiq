package com.pkmprojects.shoppiq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Configuration properties for per-cache TTL values in milliseconds.
 *
 * <p>This record binds to the {@code app.cache.*} prefix in
 * {@code application.yaml} and provides a map of cache names to their
 * time-to-live (TTL) values in milliseconds. Each entry in the
 * {@code ttl} map corresponds to a named cache used in
 * {@code @Cacheable} annotations throughout the application. This design
 * allows operators to tune cache lifetimes per environment and per cache
 * without modifying code.</p>
 *
 * <p>The TTL values are consumed by cache managers and custom cache
 * configuration to control how long cached data remains valid. Shorter
 * TTLs improve data freshness at the cost of more frequent cache misses,
 * while longer TTLs improve read performance at the cost of potentially
 * serving stale data. The appropriate balance depends on how frequently
 * the underlying data changes.</p>
 *
 * @param ttl map of cache name to TTL in milliseconds
 * @author prabhatkrmishra
 * @see CacheConfig
 * @since 1.4.0
 */
@ConfigurationProperties(prefix = "app.cache")
public record CacheProperties(
        Map<String, Long> ttl
) {
}
