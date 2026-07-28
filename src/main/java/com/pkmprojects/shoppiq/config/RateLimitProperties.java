package com.pkmprojects.shoppiq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> {@code @ConfigurationProperties}
 * class bound to {@code app.rate-limit} in {@code application.yaml}.
 *
 * <p>Defines path-based rate limit rules using the token-bucket algorithm.
 * Each {@link Rule} specifies a path pattern, capacity (limit), refill
 * window duration, and keying strategy. Key types include {@link KeyType#IP}
 * (client address) for unauthenticated endpoints and
 * {@link KeyType#USER_IP} (composite userId:ip) for authenticated
 * critical endpoints.</p>
 *
 * @author prabhatkrmishra
 * @since 0.5.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    /**
     * Master switch to enable or disable rate limiting entirely.
     */
    private boolean enabled = true;

    /**
     * Ordered list of rate limit rules evaluated by the filter.
     */
    private List<Rule> rules = new ArrayList<>();

    /**
     * Determines how the bucket key is resolved for a given request.
     */
    public enum KeyType {
        /** Rate limit by the client's IP address only. */
        IP,
        /** Rate limit by a composite {@code userId:ip} key. */
        USER_IP
    }

    /**
     * A single rate limit rule mapping a request path to a token-bucket
     * configuration.
     */
    @Getter
    @Setter
    public static class Rule {

        /**
         * Spring {@link org.springframework.web.util.pattern.PathPattern}
         * expression to match against the request URI.
         */
        private String path;

        /**
         * Maximum number of tokens (requests) allowed within the window.
         */
        private int limit;

        /**
         * Refill window duration in seconds.
         */
        private long duration;

        /**
         * Keying strategy for this rule.
         */
        private KeyType keyType = KeyType.IP;
    }
}
