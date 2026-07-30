/**
 * Centralized Spring Boot configuration classes for the Shoppiq application.
 *
 * <p>This package contains all {@code @Configuration} and
 * {@code @ConfigurationProperties} classes that define the application's
 * bean wiring, externalized property binding, and framework customization
 * points. Each configuration class is responsible for a single concern and
 * follows the single-responsibility principle to keep the context clean
 * and testable.</p>
 *
 * <p>The configuration layer bridges Spring Boot auto-configuration with
 * application-specific requirements. It defines beans for security filter
 * chains, caching infrastructure, CORS policies, Jackson serialization,
 * JPA auditing, rate limiting, REST clients, payment gateway properties,
 * email template engines, and checkout business rules. Properties are
 * bound from {@code application.yaml} under the {@code app.*} and
 * {@code shoppiq.*} prefixes.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.config;
