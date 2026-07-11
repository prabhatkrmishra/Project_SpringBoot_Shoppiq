package com.pkmprojects.shoppiq.config;

import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.filter.RateLimitFilter;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditional configuration that registers the {@link RateLimitFilter}
 * bean only when {@code app.rate-limit.enabled=true} (or when the
 * property is absent, matching the production default).
 *
 * <p>In the {@code test} profile the property is explicitly set to
 * {@code false}, so this configuration class never activates and no
 * {@code RateLimitFilter} bean is created — avoiding unsatisfied
 * dependencies in {@code @WebMvcTest} slices.</p>
 *
 * @author PrabhatKrMishra
 * @since 0.5.0
 */
@Configuration
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitFilterConfig {

    @Bean
    public RateLimitFilter rateLimitFilter(RateLimitProperties properties,
                                           JwtAuthenticationUtils jwtAuthenticationUtils,
                                           ProblemDetailResponseWriter responseWriter) {
        return new RateLimitFilter(properties, jwtAuthenticationUtils, responseWriter);
    }
}
