package com.pkmprojects.shoppiq.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * <strong>Spring Boot Concept:</strong> {@code @Configuration} class that
 * registers a {@link org.springframework.web.cors.CorsConfigurationSource}
 * bean when {@code app.cors.enabled=true}.
 *
 * <p>When enabled, the bean is injected into {@link SecurityConfig} via
 * {@code Optional<CorsConfigurationSource>} and wired into the
 * {@code SecurityFilterChain} with {@code .cors()}, ensuring CORS
 * processing happens before the JWT authentication filter so preflight
 * {@code OPTIONS} requests are handled correctly.</p>
 *
 * @author prabhatkrmishra
 * @since 0.5.0
 */
@Configuration
@ConditionalOnProperty(name = "app.cors.enabled", havingValue = "true")
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    /**
     * Creates a CORS configuration source from externalized properties.
     *
     * <p>Configures allowed origins, methods, headers, exposed headers,
     * credentials support, and preflight cache duration from
     * {@link CorsProperties}.</p>
     *
     * @param properties the CORS configuration properties
     * @return a configured {@link UrlBasedCorsConfigurationSource}
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(properties.getAllowedOrigins());
        configuration.setAllowedMethods(properties.getAllowedMethods());
        configuration.setAllowedHeaders(properties.getAllowedHeaders());
        configuration.setExposedHeaders(properties.getExposedHeaders());
        configuration.setAllowCredentials(properties.isAllowCredentials());
        configuration.setMaxAge(properties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
