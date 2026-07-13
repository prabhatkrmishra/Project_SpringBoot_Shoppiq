package com.pkmprojects.shoppiq.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Conditional CORS configuration that registers a
 * {@link org.springframework.web.cors.CorsConfigurationSource} bean
 * only when {@code app.cors.enabled=true}.
 *
 * <p>When disabled (the default), this class is not loaded by the Spring
 * context and no CORS filter is added to the security filter chain.
 * The application continues to treat every request as same-origin —
 * exactly as it does today.</p>
 *
 * <h4>When enabled</h4>
 * <p>The {@link CorsConfigurationSource} bean is injected into
 * {@link SecurityConfig} via {@code Optional<CorsConfigurationSource>},
 * which wires it into the {@code SecurityFilterChain} with
 * {@code .cors()}. This ensures CORS processing happens
 * <em>before</em> the JWT authentication filter, so preflight
 * {@code OPTIONS} requests (which carry no cookies) are handled
 * correctly.</p>
 *
 * <h4>Enabling for separate-frontend deployments</h4>
 * <pre>
 *   app.cors.enabled=true
 *   app.cors.allowed-origins=https://app.example.com,https://admin.example.com
 * </pre>
 *
 * @author PrabhatKrMishra
 * @see CorsProperties
 * @see SecurityConfig
 * @since 0.5.0
 */
@Configuration
@ConditionalOnProperty(name = "app.cors.enabled", havingValue = "true")
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

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
