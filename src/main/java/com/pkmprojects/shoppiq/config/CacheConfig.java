package com.pkmprojects.shoppiq.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * <strong>Spring Boot Concept:</strong> Enables Spring's annotation-driven cache management.
 *
 * <p>Provides a simple in-memory cache using {@code ConcurrentMapCacheManager}.
 * For production deployments, consider switching to Redis via
 * {@code spring-boot-starter-data-redis} and configuring
 * {@code RedisCacheManager} instead.</p>
 *
 * <p><strong>Cache names used:</strong></p>
 * <ul>
 *   <li>{@code banners} — active homepage banners (TTL: 1 hour)</li>
 *   <li>{@code categories} — category listings (TTL: 2 hours)</li>
 *   <li>{@code items} — item details (TTL: 30 minutes)</li>
 * </ul>
 *
 * <p>TTL values are externalized via {@link CacheProperties} under
 * {@code app.cache.ttl.*} and are available for custom {@code CacheManager}
 * configuration when migrating to Redis.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {
}