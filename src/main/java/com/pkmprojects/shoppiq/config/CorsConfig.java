package com.pkmprojects.shoppiq.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Registers a {@link CorsConfigurationSource} bean when CORS is enabled.
 *
 * <p>This configuration is conditionally activated only when the property
 * {@code app.cors.enabled} is set to {@code true}. When active, it creates
 * a {@link UrlBasedCorsConfigurationSource} bean that is injected into
 * {@link SecurityConfig} to handle CORS preflight requests and response
 * headers. This separation allows deployments where the frontend is served
 * from the same origin to disable CORS entirely.</p>
 *
 * <p>The CORS configuration is fully externalized through
 * {@link CorsProperties}, which binds to the {@code app.cors.*} prefix.
 * This includes allowed origins (supporting both exact domains and wildcard
 * patterns), HTTP methods, request and response headers, credentials
 * support, and preflight cache duration. The use of
 * {@code allowedOriginPatterns} instead of {@code allowedOrigins} is
 * required when {@code allowCredentials} is {@code true}, as the CORS
 * specification prohibits wildcard origins with credentials.</p>
 *
 * @author prabhatkrmishra
 * @see CorsProperties
 * @see SecurityConfig
 * @since 0.5.0
 */
@Configuration
@ConditionalOnProperty(name = "app.cors.enabled", havingValue = "true")
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    /**
     * Creates a CORS configuration source from externalized properties.
     *
     * <p>The returned {@link UrlBasedCorsConfigurationSource} applies the
     * same CORS policy to all request paths ({@code /**}). It is registered
     * as a bean so that {@link SecurityConfig} can inject it into the HTTP
     * security builder's CORS handler. The configuration is derived entirely
     * from {@link CorsProperties}, allowing operators to adjust CORS
     * behavior through {@code application.yaml} without modifying code.</p>
     *
     * @param properties the CORS configuration properties bound from {@code app.cors.*}
     * @return a fully configured {@link UrlBasedCorsConfigurationSource}
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
