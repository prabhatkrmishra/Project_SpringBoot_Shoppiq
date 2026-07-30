package com.pkmprojects.shoppiq.filter;

import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.config.RateLimitProperties;
import com.pkmprojects.shoppiq.config.RateLimitProperties.KeyType;
import com.pkmprojects.shoppiq.config.RateLimitProperties.Rule;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.exception.factory.ProblemDetailFactory;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.PathContainer;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Filter that enforces per-path token-bucket rate limiting using Bucket4j.
 *
 * <p>Placed before the JWT authentication filter so abusive traffic is
 * rejected before authentication processing. Supports IP-based and
 * user-IP-based bucket keys. The filter is conditionally registered via
 * {@link com.pkmprojects.shoppiq.config.RateLimitFilterConfig} based on
 * the {@code app.rate-limit.enabled} property.</p>
 *
 * <p>The filter evaluates configured rules in order and applies the first
 * matching rule to a given request. If no rule matches, the request is
 * allowed through without throttling. When a rate limit is exceeded, the
 * filter returns a 429 Too Many Requests response with a Retry-After
 * header and RFC 9457 Problem Detail body.</p>
 *
 * @author prabhatkrmishra
 * @see RateLimitProperties
 * @since 0.5.0
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final long EVICT_AFTER_SECONDS = 3600;
    private static final long EVICT_INTERVAL_SECONDS = 300;
    private static final int MAX_BUCKETS = 10_000;
    private final RateLimitProperties properties;
    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final ProblemDetailResponseWriter responseWriter;
    private final Clock clock;
    private final Map<PathPattern, Rule> ruleIndex = new ConcurrentHashMap<>();
    private final Map<String, BucketEntry> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService evictor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limit-evictor");
        t.setDaemon(true);
        return t;
    });

    /**
     * Constructs the filter, parses path patterns from the rule
     * configuration, and starts the bucket eviction scheduler.
     *
     * @param properties             rate limit configuration
     * @param jwtAuthenticationUtils utility for JWT operations
     * @param responseWriter         writer for RFC 9457 error responses
     * @param clock                  injectable clock for testability
     */
    public RateLimitFilter(RateLimitProperties properties,
                           JwtAuthenticationUtils jwtAuthenticationUtils,
                           ProblemDetailResponseWriter responseWriter,
                           Clock clock) {
        this.properties = properties;
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.responseWriter = responseWriter;
        this.clock = clock;

        PathPatternParser parser = new PathPatternParser();
        for (Rule rule : properties.getRules()) {
            PathPattern pattern = parser.parse(rule.getPath());
            ruleIndex.put(pattern, rule);
            logger.debug("Registered rate limit rule: {} ({} per {}s, key={})",
                    rule.getPath(), rule.getLimit(), rule.getDuration(), rule.getKeyType());
        }

        evictor.scheduleAtFixedRate(this::evictStaleBuckets,
                EVICT_INTERVAL_SECONDS, EVICT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Formats a wait duration in seconds into a human-readable string
     * suitable for user-facing messages.
     *
     * @param seconds the wait duration in seconds
     * @return a human-readable string (e.g. "30 seconds", "15 minutes", "2 hours")
     */
    static String formatWaitDuration(long seconds) {
        if (seconds < 60) {
            return seconds + " second" + (seconds == 1 ? "" : "s");
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        }
        long hours = minutes / 60;
        return hours + " hour" + (hours == 1 ? "" : "s");
    }

    /**
     * Shuts down the bucket eviction scheduler gracefully.
     */
    @PreDestroy
    void shutdown() {
        evictor.shutdown();
        try {
            if (!evictor.awaitTermination(5, TimeUnit.SECONDS)) {
                evictor.shutdownNow();
            }
        } catch (InterruptedException _) {
            evictor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Removes buckets that have exceeded their maximum age or idle time.
     */
    private void evictStaleBuckets() {
        long nowMillis = clock.millis();
        Iterator<Map.Entry<String, BucketEntry>> it = buckets.entrySet().iterator();
        int removed = 0;
        while (it.hasNext()) {
            Map.Entry<String, BucketEntry> entry = it.next();
            long ageSeconds = TimeUnit.MILLISECONDS.toSeconds(nowMillis - entry.getValue().createdAtMillis);
            long idleSeconds = TimeUnit.MILLISECONDS.toSeconds(nowMillis - entry.getValue().lastAccessedAtMillis);
            if (ageSeconds > EVICT_AFTER_SECONDS || idleSeconds > EVICT_AFTER_SECONDS / 2) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            logger.debug("Evicted {} stale rate-limit buckets ({} remaining)", removed, buckets.size());
        }
    }

    /**
     * Applies rate limiting to the incoming request by matching its path
     * against configured rules and consuming a token from the corresponding
     * bucket.
     *
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if filter processing fails
     * @throws IOException      if writing the error response fails
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestUri = request.getRequestURI();
        PathContainer pathContainer = PathContainer.parsePath(requestUri);

        for (Map.Entry<PathPattern, Rule> entry : ruleIndex.entrySet()) {
            if (entry.getKey().matches(pathContainer)) {
                Rule rule = entry.getValue();
                String bucketKey = resolveBucketKey(request, rule);

                if (!bucketKey.isEmpty()) {
                    Bucket bucket = resolveBucket(bucketKey, rule);
                    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
                    touchBucket(bucketKey);

                    if (probe.isConsumed()) {
                        response.setHeader("X-Rate-Limit-Remaining",
                                String.valueOf(probe.getRemainingTokens()));
                    } else {
                        long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
                        response.setHeader("Retry-After", String.valueOf(waitSeconds));
                        response.setHeader("X-Rate-Limit-Retry-After-Seconds",
                                String.valueOf(waitSeconds));

                        String waitMessage = formatWaitDuration(waitSeconds);
                        ProblemDetail problemDetail = ProblemDetailFactory.create(
                                HttpStatus.TOO_MANY_REQUESTS,
                                "Too many attempts. Please wait " + waitMessage + " before trying again.",
                                ErrorCode.RATE_LIMIT_EXCEEDED,
                                URI.create(requestUri)
                        );
                        responseWriter.write(response, problemDetail);
                        return;
                    }
                }
                break;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Resolves the bucket key for the current request based on the rule's
     * {@link KeyType}.
     *
     * @param request the incoming HTTP request
     * @param rule    the matched rate limit rule
     * @return the bucket key, or {@code null} if the key cannot be resolved
     * (e.g. USER_IP rule but no valid JWT present)
     */
    private String resolveBucketKey(HttpServletRequest request, Rule rule) {
        String remoteAddr = request.getRemoteAddr();

        return switch (rule.getKeyType()) {
            case IP -> "ip:" + remoteAddr;
            case USER_IP -> {
                String token = jwtAuthenticationUtils.extractJwtFromCookies(request);
                if (token == null) {
                    yield "ip:" + remoteAddr;
                }
                try {
                    Long userId = jwtAuthenticationUtils.getUserIdFromToken(token);
                    yield "uid:" + userId + ":ip:" + remoteAddr;
                } catch (Exception _) {
                    yield "ip:" + remoteAddr;
                }
            }
        };
    }

    /**
     * Retrieves an existing bucket for the given key or creates a new one
     * with the bandwidth defined by the rule.
     *
     * @param key  the bucket key
     * @param rule the rate limit rule defining capacity and refill
     * @return the bucket instance
     */
    private Bucket resolveBucket(String key, Rule rule) {
        BucketEntry existing = buckets.get(key);
        if (existing != null) {
            return existing.bucket;
        }
        if (buckets.size() >= MAX_BUCKETS) {
            evictStaleBuckets();
            if (buckets.size() >= MAX_BUCKETS) {
                logger.warn("Rate-limit bucket map full ({} entries); rejecting request for key={}", buckets.size(), key);
                return Bucket.builder()
                        .addLimit(Bandwidth.classic(0, Refill.greedy(0, Duration.ofHours(1))))
                        .build();
            }
        }
        Bandwidth bandwidth = Bandwidth.classic(
                rule.getLimit(),
                Refill.greedy(rule.getLimit(), Duration.ofSeconds(rule.getDuration()))
        );
        return buckets.computeIfAbsent(key, k -> new BucketEntry(Bucket.builder().addLimit(bandwidth).build(), clock.millis(), clock.millis()))
                .bucket;
    }

    /**
     * Updates the last-accessed timestamp of a bucket entry to prevent
     * premature eviction.
     *
     * @param key the bucket key
     */
    private void touchBucket(String key) {
        BucketEntry entry = buckets.get(key);
        if (entry != null) {
            entry.lastAccessedAtMillis = clock.millis();
        }
    }

    /**
     * Skips rate limiting when the feature is disabled or no rules are
     * configured.
     *
     * @param request the HTTP request
     * @return {@code true} if the filter should be skipped
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.isEnabled() || ruleIndex.isEmpty();
    }

    private static final class BucketEntry {
        final Bucket bucket;
        final long createdAtMillis;
        volatile long lastAccessedAtMillis;

        BucketEntry(Bucket bucket, long createdAtMillis, long lastAccessedAtMillis) {
            this.bucket = bucket;
            this.createdAtMillis = createdAtMillis;
            this.lastAccessedAtMillis = lastAccessedAtMillis;
        }
    }
}
