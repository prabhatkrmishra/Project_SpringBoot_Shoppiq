package com.pkmprojects.shoppiq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> {@code @ConfigurationProperties}
 * class bound to {@code app.cors.*} in {@code application.yaml}.
 *
 * <p>Controls CORS behavior for separate-frontend deployments. When
 * {@link #enabled} is {@code false} (default), no CORS filter is registered
 * and all requests are treated as same-origin. Uses
 * {@code allowedOriginPatterns} rather than {@code allowedOrigins} to
 * remain compatible with {@code allowCredentials=true} per the CORS spec.</p>
 *
 * @author prabhatkrmishra
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
