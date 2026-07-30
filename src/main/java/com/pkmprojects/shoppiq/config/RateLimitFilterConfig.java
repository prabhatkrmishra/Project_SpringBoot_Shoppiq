package com.pkmprojects.shoppiq.config;

import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.filter.RateLimitFilter;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Conditionally registers the {@link RateLimitFilter} bean based on configuration.
 *
 * <p>This configuration class creates the {@link RateLimitFilter} only when
 * the property {@code app.rate-limit.enabled} is set to {@code true} (or
 * omitted, as it defaults to {@code true}). The conditional registration
 * ensures that the rate-limiting filter is cleanly excluded in test
 * profiles and in deployments where rate limiting is handled at the
 * infrastructure layer (e.g., API gateway or load balancer).</p>
 *
 * <p>The filter bean receives its configuration through
 * {@link RateLimitProperties}, which binds to the {@code app.rate-limit}
 * prefix in {@code application.yaml}. It also requires the
 * {@link JwtAuthenticationUtils} for extracting user identity from JWT
 * tokens and the {@link ProblemDetailResponseWriter} for producing
 * RFC 9457-compliant 429 responses when limits are exceeded.</p>
 *
 * @author prabhatkrmishra
 * @see RateLimitProperties
 * @see RateLimitFilter
 * @since 0.5.0
 */
@Configuration
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitFilterConfig {

    /**
     * Creates the rate limit filter bean with externalized properties.
     *
     * <p>The filter is instantiated with its four dependencies injected
     * explicitly rather than relying on field injection. The injectable
     * {@link Clock} parameter allows tests to control time progression
     * deterministically, which is essential for verifying token-bucket
     * refill behavior without real wall-clock delays.</p>
     *
     * @param properties             the rate limit configuration properties
     * @param jwtAuthenticationUtils utility for extracting JWT user information
     * @param responseWriter         utility for writing RFC 9457 Problem Detail responses
     * @param clock                  injectable clock for deterministic testing
     * @return a configured {@link RateLimitFilter} instance
     */
    @Bean
    public RateLimitFilter rateLimitFilter(RateLimitProperties properties,
                                           JwtAuthenticationUtils jwtAuthenticationUtils,
                                           ProblemDetailResponseWriter responseWriter,
                                           Clock clock) {
        return new RateLimitFilter(properties, jwtAuthenticationUtils, responseWriter, clock);
    }
}
