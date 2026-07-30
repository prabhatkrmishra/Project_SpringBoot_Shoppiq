package com.pkmprojects.shoppiq.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring's annotation-driven cache management with per-cache TTL control.
 *
 * <p>This configuration class activates the {@code @EnableCaching}
 * infrastructure, allowing any Spring-managed bean to use
 * {@code @Cacheable}, {@code @CacheEvict}, and {@code @CachePut}
 * annotations. The actual cache implementation is provided by Spring Boot's
 * autoconfiguration (defaulting to a concurrent hash map in development)
 * and can be swapped to Redis or Caffeine via dependency changes.</p>
 *
 * <p>Cache TTL values are externalized through {@link CacheProperties},
 * which binds to the {@code app.cache.ttl} map in {@code application.yaml}.
 * Each entry in the map corresponds to a named cache (e.g., "banners",
 * "categories", "items") and specifies the time-to-live in milliseconds.
 * This design allows operators to tune cache lifetimes per environment
 * without code changes.</p>
 *
 * @author prabhatkrmishra
 * @see CacheProperties
 * @since 1.4.0
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {
}