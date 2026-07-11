package com.pkmprojects.shoppiq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the rate limiting subsystem.
 *
 * <p>Reads the {@code app.rate-limit} block from
 * {@code application.yaml} and exposes it as a strongly typed bean.
 * Each {@link Rule} defines a single path-based rate limit with its
 * capacity, refill window, and keying strategy.</p>
 *
 * <h4>Key types</h4>
 * <ul>
 *   <li>{@link KeyType#IP} — the client's remote address is used as the
 *       bucket key. Suitable for unauthenticated endpoints where the
 *       caller identity is not known.</li>
 *   <li>{@link KeyType#USER_IP} — a composite key of
 *       {@code userId:remoteAddr} is used. Suitable for authenticated
 *       critical endpoints where both the user and the originating IP
 *       must be tracked.</li>
 * </ul>
 *
 * <h4>Disabled mode</h4>
 * <p>When {@link #enabled} is {@code false}, the rate limit filter
 * skips all checks. This is intended for test profiles.</p>
 *
 * @author PrabhatKrMishra
 * @since 0.5.0
 * @see com.pkmprojects.shoppiq.filter.RateLimitFilter
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
