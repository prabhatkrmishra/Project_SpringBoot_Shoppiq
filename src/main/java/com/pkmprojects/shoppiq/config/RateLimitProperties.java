package com.pkmprojects.shoppiq.config;

import com.pkmprojects.shoppiq.filter.RateLimitFilter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for path-based rate limiting rules.
 *
 * <p>This class binds to the {@code app.rate-limit} prefix in
 * {@code application.yaml} and defines the token-bucket rate limiting
 * configuration used by {@link com.pkmprojects.shoppiq.filter.RateLimitFilter}.
 * Rate limiting is a critical defense layer that protects the application
 * from abuse, brute-force attacks, and resource exhaustion by controlling
 * how many requests a client can make within a given time window.</p>
 *
 * <p>The configuration supports a list of ordered rules, each mapping a
 * Spring path pattern to a token-bucket configuration with a specific
 * capacity, refill duration, and keying strategy. Rules are evaluated
 * in order, and the first matching rule applies to a given request.
 * The master {@link #enabled} switch allows rate limiting to be disabled
 * entirely at the property level.</p>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.filter.RateLimitFilter
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
     *
     * <p>The keying strategy controls the granularity of rate limiting.
     * Using {@link #IP} limits each client IP independently, which is
     * appropriate for public endpoints. Using {@link #USER_IP} combines
     * the authenticated user ID with the IP address, providing per-user
     * limits while still distinguishing between requests from different
     * machines under the same account.</p>
     */
    public enum KeyType {
        /**
         * Rate limit by the client's IP address only.
         *
         * <p>Anonymous and authenticated requests from the same IP share
         * the same token bucket. This is suitable for public endpoints
         * where user identity is not available.</p>
         */
        IP,
        /**
         * Rate limit by a composite {@code userId:ip} key.
         *
         * <p>Authenticated users get their own bucket per IP, while
         * anonymous users fall back to IP-only keying. This provides
         * per-user fairness without penalizing multiple users behind
         * a shared NAT or proxy.</p>
         */
        USER_IP
    }

    /**
     * A single rate limit rule mapping a request path to a token-bucket
     * configuration.
     *
     * <p>Each rule defines a path pattern, a maximum number of tokens
     * (requests) allowed within the refill window, the duration of
     * that window in seconds, and the keying strategy. The
     * {@link RateLimitFilter} evaluates rules in order and applies
     * the first matching rule to a given request. If no rule matches,
     * the request is allowed through without throttling.</p>
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
