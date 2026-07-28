package com.pkmprojects.shoppiq.config;

import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.filter.RateLimitFilter;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <strong>Spring Boot Concept:</strong> {@code @Configuration} class that
 * conditionally registers the {@link RateLimitFilter} bean when
 * {@code app.rate-limit.enabled=true} (default, matching production).
 *
 * <p>In the {@code test} profile the property is explicitly set to
 * {@code false}, so no {@code RateLimitFilter} bean is created —
 * avoiding unsatisfied dependencies in {@code @WebMvcTest} slices.</p>
 *
 * @author prabhatkrmishra
 * @since 0.5.0
 */
@Configuration
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitFilterConfig {

    /**
     * Creates the rate limit filter bean with externalized properties.
     *
     * @param properties            the rate limit configuration
     * @param jwtAuthenticationUtils utility for extracting JWT user information
     * @param responseWriter        utility for writing RFC 9457 responses
     * @return a configured {@link RateLimitFilter} instance
     */
    @Bean
    public RateLimitFilter rateLimitFilter(RateLimitProperties properties,
                                           JwtAuthenticationUtils jwtAuthenticationUtils,
                                           ProblemDetailResponseWriter responseWriter) {
        return new RateLimitFilter(properties, jwtAuthenticationUtils, responseWriter);
    }
}
