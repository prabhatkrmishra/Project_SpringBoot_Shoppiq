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
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.PathContainer;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.net.URI;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting filter that enforces per-path token-bucket quotas using
 * Bucket4j.
 *
 * <p>Placed before {@link com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter}
 * in the security filter chain so that abusive traffic is rejected before any
 * authentication processing occurs.</p>
 *
 * <h4>Key resolution</h4>
 * <ul>
 *   <li>{@link KeyType#IP} — bucket key is the client's remote address.
 *       Used for unauthenticated endpoints (login, register, etc.).</li>
 *   <li>{@link KeyType#USER_IP} — bucket key is {@code userId:ip}.
 *       Used for authenticated critical endpoints (checkout, payment,
 *       password change). Requires a valid JWT to extract the user ID;
 *       if no JWT is present the request falls through unauthenticated
 *       and is not rate-limited by this rule.</li>
 * </ul>
 *
 * <h4>Error response</h4>
 * <p>When a bucket is exhausted, the filter writes an RFC 9457
 * {@link ProblemDetail} with status {@code 429 Too Many Requests} and a
 * {@code Retry-After} header indicating the seconds until the next token
 * becomes available.</p>
 *
 * <h4>Bucket lifecycle</h4>
 * <p>Buckets are stored in a {@link ConcurrentHashMap} and created lazily
 * per key. Expired buckets are evicted periodically to prevent unbounded
 * memory growth.</p>
 *
 * @author PrabhatKrMishra
 * @since 0.5.0
 * @see RateLimitProperties
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitProperties properties;
    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final ProblemDetailResponseWriter responseWriter;

    private static final long EVICT_AFTER_SECONDS = 3600;
    private static final long EVICT_INTERVAL_SECONDS = 300;

    private final Map<String, Rule> ruleIndex = new ConcurrentHashMap<>();
    private final Map<String, BucketEntry> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService evictor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limit-evictor");
        t.setDaemon(true);
        return t;
    });

    public RateLimitFilter(RateLimitProperties properties,
                           JwtAuthenticationUtils jwtAuthenticationUtils,
                           ProblemDetailResponseWriter responseWriter) {
        this.properties = properties;
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.responseWriter = responseWriter;

        PathPatternParser parser = new PathPatternParser();
        for (Rule rule : properties.getRules()) {
            PathPattern pattern = parser.parse(rule.getPath());
            ruleIndex.put(rule.getPath(), rule);
            logger.debug("Registered rate limit rule: {} ({} per {}s, key={})",
                    rule.getPath(), rule.getLimit(), rule.getDuration(), rule.getKeyType());
        }

        evictor.scheduleAtFixedRate(this::evictStaleBuckets,
                EVICT_INTERVAL_SECONDS, EVICT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private record BucketEntry(Bucket bucket, long createdAt) {}

    private void evictStaleBuckets() {
        long now = System.nanoTime();
        Iterator<Map.Entry<String, BucketEntry>> it = buckets.entrySet().iterator();
        int removed = 0;
        while (it.hasNext()) {
            Map.Entry<String, BucketEntry> entry = it.next();
            long ageSeconds = TimeUnit.NANOSECONDS.toSeconds(now - entry.getValue().createdAt());
            if (ageSeconds > EVICT_AFTER_SECONDS) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            logger.debug("Evicted {} stale rate-limit buckets ({} remaining)", removed, buckets.size());
        }
    }

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

        for (Map.Entry<String, Rule> entry : ruleIndex.entrySet()) {
            PathPattern pattern = new PathPatternParser().parse(entry.getKey());
            if (pattern.matches(pathContainer)) {
                Rule rule = entry.getValue();
                String bucketKey = resolveBucketKey(request, rule);

                if (bucketKey != null) {
                    Bucket bucket = resolveBucket(bucketKey, rule);
                    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

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
                } catch (Exception e) {
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
        BucketEntry entry = buckets.computeIfAbsent(key, k -> {
            Bandwidth bandwidth = Bandwidth.classic(
                    rule.getLimit(),
                    Refill.greedy(rule.getLimit(), Duration.ofSeconds(rule.getDuration()))
            );
            return new BucketEntry(Bucket.builder().addLimit(bandwidth).build(), System.nanoTime());
        });
        return entry.bucket();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.isEnabled() || ruleIndex.isEmpty();
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
}
