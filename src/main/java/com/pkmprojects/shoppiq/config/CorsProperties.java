package com.pkmprojects.shoppiq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Externalised CORS configuration properties.
 *
 * <p>Bound to {@code app.cors.*} in {@code application.yaml}. When
 * {@link #enabled} is {@code false} (the default), no CORS filter is
 * registered in the Spring Security filter chain and the application
 * behaves exactly as it does today — all requests are treated as
 * same-origin.</p>
 *
 * <h4>Enabling for separate-frontend deployments</h4>
 * <p>Set {@code app.cors.enabled=true} and provide the list of
 * allowed origins. Origins are registered via
 * {@link org.springframework.web.cors.CorsConfiguration#setAllowedOriginPatterns(List)}
 * rather than {@code setAllowedOrigins}, which is required when
 * {@link #allowCredentials} is {@code true} — the CORS spec forbids
 * a wildcard origin with credentialed requests.</p>
 *
 * <h4>Environment variables</h4>
 * <pre>
 *   CORS_ENABLED=true
 *   CORS_ALLOWED_ORIGINS=https://app.example.com,https://admin.example.com
 * </pre>
 *
 * @author PrabhatKrMishra
 * @see CorsConfig
 * @since 0.5.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /**
     * Master switch. When {@code false}, no {@code CorsConfigurationSource}
     * bean is created and Spring Security has no CORS support.
     */
    private boolean enabled = false;

    /**
     * Origins allowed to make cross-origin requests.
     * <p>Supports exact domains ({@code https://app.example.com}) and
     * patterns ({@code https://*.example.com}). Mapped to
     * {@code allowedOriginPatterns} to remain compatible with
     * {@code allowCredentials=true}.</p>
     */
    private List<String> allowedOrigins = List.of("http://localhost:3000");

    /**
     * HTTP methods permitted in cross-origin requests.
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");

    /**
     * Request headers the browser is allowed to send.
     * <p>Must include {@code Authorization} when using bearer tokens
     * or cookie-based JWT authentication from a separate frontend.</p>
     */
    private List<String> allowedHeaders = List.of(
            "Authorization", "Content-Type", "X-Requested-With",
            "Accept", "Origin", "Cache-Control", "X-Request-Id"
    );

    /**
     * Response headers exposed to browser JavaScript.
     */
    private List<String> exposedHeaders = List.of("X-Request-Id");

    /**
     * Whether to allow credentials (cookies, authorization headers).
     * <p>Must be {@code true} for the JWT-in-HttpOnly-cookie
     * authentication model used by this application.</p>
     */
    private boolean allowCredentials = true;

    /**
     * How long (in seconds) the browser may cache a preflight response.
     * <p>Higher values reduce preflight traffic for stable APIs.
     * {@code 3600} (1 hour) is a reasonable default.</p>
     */
    private long maxAge = 3600;
}
