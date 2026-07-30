package com.pkmprojects.shoppiq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for CORS behavior in separate-frontend deployments.
 *
 * <p>This class binds to the {@code app.cors.*} prefix in
 * {@code application.yaml} and defines the Cross-Origin Resource Sharing
 * policy for the application. CORS is essential when the frontend and
 * backend are deployed on different origins (e.g., a React SPA on port 3000
 * and the API on port 8080). When {@link #enabled} is {@code false}, no
 * CORS filter is registered, which is appropriate for same-origin
 * deployments where the frontend is served from the same origin as the
 * API.</p>
 *
 * <p>The configuration uses {@code allowedOriginPatterns} instead of
 * {@code allowedOrigins} to remain compatible with
 * {@code allowCredentials=true}, which is required for the JWT-in-HttpOnly-cookie
 * authentication model. The {@link #maxAge} property controls how long
 * the browser caches preflight responses, reducing unnecessary OPTIONS
 * requests for stable APIs.</p>
 *
 * @author prabhatkrmishra
 * @see CorsConfig
 * @since 0.5.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /**
     * Master switch for CORS support.
     *
     * <p>When set to {@code false}, no {@link org.springframework.web.cors.CorsConfigurationSource}
     * bean is created, and Spring Security has no CORS support. This is
     * appropriate for same-origin deployments where the frontend is served
     * from the same origin as the API.</p>
     */
    private boolean enabled = false;

    /**
     * Origins allowed to make cross-origin requests.
     *
     * <p>Supports exact domains (e.g., {@code "https://app.example.com"})
     * and wildcard patterns (e.g., {@code "https://*.example.com"}). These
     * are mapped to Spring's {@code allowedOriginPatterns} property to
     * remain compatible with {@code allowCredentials=true}. The default
     * value allows the local development frontend at port 3000.</p>
     */
    private List<String> allowedOrigins = List.of("http://localhost:3000");

    /**
     * HTTP methods permitted in cross-origin requests.
     *
     * <p>Includes all standard REST methods (GET, POST, PUT, DELETE, PATCH)
     * plus OPTIONS for preflight requests. This list should be kept
     * restrictive to prevent abuse while supporting the application's
     * API contract.</p>
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");

    /**
     * Request headers the browser is allowed to send in cross-origin requests.
     *
     * <p>Must include {@code Authorization} when using bearer tokens or
     * cookie-based JWT authentication from a separate frontend. The
     * {@code X-Request-Id} header is included to support distributed
     * tracing across frontend and backend.</p>
     */
    private List<String> allowedHeaders = List.of(
            "Authorization", "Content-Type", "X-Requested-With",
            "Accept", "Origin", "Cache-Control", "X-Request-Id"
    );

    /**
     * Response headers exposed to browser JavaScript.
     *
     * <p>By default, browsers only expose a limited set of response headers
     * to JavaScript. This list adds {@code X-Request-Id} so that the
     * frontend can read the request ID for logging and debugging purposes.</p>
     */
    private List<String> exposedHeaders = List.of("X-Request-Id");

    /**
     * Whether to allow credentials (cookies, authorization headers) in
     * cross-origin requests.
     *
     * <p>Must be {@code true} for the JWT-in-HttpOnly-cookie authentication
     * model used by this application. When enabled, the browser includes
     * cookies in cross-origin requests and JavaScript can access the
     * authorization header.</p>
     */
    private boolean allowCredentials = true;

    /**
     * How long (in seconds) the browser may cache a preflight response.
     *
     * <p>Higher values reduce preflight traffic for stable APIs. The default
     * of {@code 3600} (1 hour) is a reasonable balance between reducing
     * unnecessary OPTIONS requests and allowing timely policy changes.</p>
     */
    private long maxAge = 3600;
}
