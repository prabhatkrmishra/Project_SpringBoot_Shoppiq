package com.pkmprojects.shoppiq.filter;

import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.config.RateLimitProperties;
import com.pkmprojects.shoppiq.config.RateLimitProperties.KeyType;
import com.pkmprojects.shoppiq.config.RateLimitProperties.Rule;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RateLimitFilter}.
 *
 * <p>All external dependencies ({@link JwtAuthenticationUtils},
 * {@link ProblemDetailResponseWriter}) are mocked. No Spring context
 * or database is involved.</p>
 *
 * <h2>Coverage</h2>
 * <ul>
 *     <li>shouldNotFilter() — disabled property, empty rules, enabled with rules</li>
 *     <li>doFilterInternal() — allowed requests, blocked (429) responses,
 *         key resolution (IP, USER_IP with JWT, fallback to IP)</li>
 *     <li>evictStaleBuckets() — stale bucket removal</li>
 *     <li>formatWaitDuration() — seconds, minutes, hours formatting</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitFilter Tests")
class RateLimitFilterTest {

    @Mock
    private JwtAuthenticationUtils jwtAuthUtils;

    @Mock
    private ProblemDetailResponseWriter responseWriter;

    @Mock
    private FilterChain filterChain;

    @Captor
    private ArgumentCaptor<ProblemDetail> problemDetailCaptor;

    private RateLimitProperties properties;
    private RateLimitFilter filter;

    // ─────────────────────────────────────────────────────────────
    // Setup
    // ─────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        properties = createProperties("/test/**", 2, 60, KeyType.IP);
        filter = new RateLimitFilter(properties, jwtAuthUtils, responseWriter);
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private static RateLimitProperties createProperties(String path, int limit,
                                                         long duration, KeyType keyType) {
        RateLimitProperties props = new RateLimitProperties();
        props.setEnabled(true);
        Rule rule = new Rule();
        rule.setPath(path);
        rule.setLimit(limit);
        rule.setDuration(duration);
        rule.setKeyType(keyType);
        props.setRules(List.of(rule));
        return props;
    }

    private static Rule createDefaultRule() {
        Rule rule = new Rule();
        rule.setPath("/test/**");
        rule.setLimit(2);
        rule.setDuration(60);
        rule.setKeyType(KeyType.IP);
        return rule;
    }

    private static MockHttpServletRequest buildRequest(String uri, String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRemoteAddr(remoteAddr);
        return request;
    }

    private static MockHttpServletResponse buildResponse() {
        return new MockHttpServletResponse();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getBuckets() throws Exception {
        Field field = RateLimitFilter.class.getDeclaredField("buckets");
        field.setAccessible(true);
        return (Map<String, Object>) field.get(filter);
    }

    private void callEvictStaleBuckets() throws Exception {
        Method method = RateLimitFilter.class.getDeclaredMethod("evictStaleBuckets");
        method.setAccessible(true);
        method.invoke(filter);
    }

    private void rebuildFilter() {
        filter = new RateLimitFilter(properties, jwtAuthUtils, responseWriter);
    }

    // ═══════════════════════════════════════════════════════════════
    // shouldNotFilter()
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("shouldNotFilter")
    class ShouldNotFilter {

        @Test
        @DisplayName("Returns true when rate limiting is disabled")
        void shouldNotFilter_disabledProperty() {
            RateLimitProperties disabled = new RateLimitProperties();
            disabled.setEnabled(false);
            disabled.setRules(List.of(createDefaultRule()));
            RateLimitFilter disabledFilter = new RateLimitFilter(disabled, jwtAuthUtils, responseWriter);

            assertThat(disabledFilter.shouldNotFilter(buildRequest("/test", "10.0.0.1"))).isTrue();
        }

        @Test
        @DisplayName("Returns true when no rules are configured")
        void shouldNotFilter_emptyRules() {
            RateLimitProperties noRules = new RateLimitProperties();
            noRules.setEnabled(true);
            noRules.setRules(List.of());
            RateLimitFilter noRulesFilter = new RateLimitFilter(noRules, jwtAuthUtils, responseWriter);

            assertThat(noRulesFilter.shouldNotFilter(buildRequest("/test", "10.0.0.1"))).isTrue();
        }

        @Test
        @DisplayName("Returns false when enabled with rules")
        void shouldNotFilter_enabledWithRules() {
            assertThat(filter.shouldNotFilter(buildRequest("/test", "10.0.0.1"))).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // doFilterInternal() — allowed
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("doFilterInternal — allowed")
    class DoFilterAllowed {

        @Test
        @DisplayName("Passes through and sets remaining header when under limit")
        void doFilterUnderLimit_setsRemainingHeader() throws Exception {
            MockHttpServletRequest request = buildRequest("/test/anything", "10.0.0.1");
            MockHttpServletResponse response = buildResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(response.getHeader("X-Rate-Limit-Remaining")).isEqualTo("1");
        }

        @Test
        @DisplayName("Passes through without rate limit headers when no rule matches")
        void doFilter_noMatchingRule_passesThrough() throws Exception {
            MockHttpServletRequest request = buildRequest("/api/products", "10.0.0.1");
            MockHttpServletResponse response = buildResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(response.getHeader("X-Rate-Limit-Remaining")).isNull();
        }

        @Test
        @DisplayName("Passes through for USER_IP rule when JWT is valid")
        void doFilter_userIpRule_withJwt_setsRemainingHeader() throws Exception {
            properties = createProperties("/test/**", 2, 60, KeyType.USER_IP);
            rebuildFilter();

            when(jwtAuthUtils.extractJwtFromCookies(any())).thenReturn("valid-token");
            when(jwtAuthUtils.getUserIdFromToken("valid-token")).thenReturn(42L);

            MockHttpServletRequest request = buildRequest("/test/anything", "10.0.0.1");
            MockHttpServletResponse response = buildResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(response.getHeader("X-Rate-Limit-Remaining")).isEqualTo("1");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // doFilterInternal() — blocked (429)
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("doFilterInternal — blocked (429)")
    class DoFilterBlocked {

        @Test
        @DisplayName("Returns 429 ProblemDetail when limit is exceeded")
        void doFilterOverLimit_returns429ProblemDetail() throws Exception {
            properties = createProperties("/test/**", 1, 60, KeyType.IP);
            rebuildFilter();

            MockHttpServletRequest request = buildRequest("/test/anything", "10.0.0.1");
            MockHttpServletResponse response = buildResponse();

            filter.doFilterInternal(request, response, filterChain);

            MockHttpServletResponse response2 = buildResponse();
            filter.doFilterInternal(request, response2, filterChain);

            verify(responseWriter).write(eq(response2), problemDetailCaptor.capture());
            verify(filterChain, never()).doFilter(any(), eq(response2));

            ProblemDetail pd = problemDetailCaptor.getValue();
            assertThat(pd.getStatus()).isEqualTo(429);
            assertThat(pd.getProperties().get("errorCode")).isEqualTo("AUTH-429-001");
        }

        @Test
        @DisplayName("Sets Retry-After header when blocked")
        void doFilterOverLimit_setsRetryAfterHeader() throws Exception {
            properties = createProperties("/test/**", 1, 60, KeyType.IP);
            rebuildFilter();

            MockHttpServletRequest request = buildRequest("/test/anything", "10.0.0.1");
            MockHttpServletResponse response = buildResponse();

            filter.doFilterInternal(request, response, filterChain);

            MockHttpServletResponse response2 = buildResponse();
            filter.doFilterInternal(request, response2, filterChain);

            assertThat(response2.getHeader("Retry-After")).isNotBlank();
            assertThat(response2.getHeader("X-Rate-Limit-Retry-After-Seconds")).isNotBlank();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // doFilterInternal() — key resolution
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("doFilterInternal — key resolution")
    class DoFilterKeyResolution {

        @Test
        @DisplayName("IP rule creates separate buckets for different IPs")
        void doFilter_ipRule_usesRemoteAddress() throws Exception {
            MockHttpServletRequest request1 = buildRequest("/test/a", "10.0.0.1");
            MockHttpServletResponse response1 = buildResponse();
            MockHttpServletRequest request2 = buildRequest("/test/b", "10.0.0.2");
            MockHttpServletResponse response2 = buildResponse();

            filter.doFilterInternal(request1, response1, filterChain);
            filter.doFilterInternal(request2, response2, filterChain);

            assertThat(response1.getHeader("X-Rate-Limit-Remaining")).isEqualTo("1");
            assertThat(response2.getHeader("X-Rate-Limit-Remaining")).isEqualTo("1");
        }

        @Test
        @DisplayName("USER_IP rule creates separate buckets for different users")
        void doFilter_userIpRule_withJwt_usesCompositeKey() throws Exception {
            properties = createProperties("/test/**", 2, 60, KeyType.USER_IP);
            rebuildFilter();

            when(jwtAuthUtils.extractJwtFromCookies(any())).thenReturn("token-42");
            when(jwtAuthUtils.getUserIdFromToken("token-42")).thenReturn(42L);

            MockHttpServletRequest request1 = buildRequest("/test/a", "10.0.0.1");
            MockHttpServletResponse response1 = buildResponse();
            filter.doFilterInternal(request1, response1, filterChain);

            when(jwtAuthUtils.extractJwtFromCookies(any())).thenReturn("token-99");
            when(jwtAuthUtils.getUserIdFromToken("token-99")).thenReturn(99L);

            MockHttpServletRequest request2 = buildRequest("/test/b", "10.0.0.1");
            MockHttpServletResponse response2 = buildResponse();
            filter.doFilterInternal(request2, response2, filterChain);

            assertThat(response1.getHeader("X-Rate-Limit-Remaining")).isEqualTo("1");
            assertThat(response2.getHeader("X-Rate-Limit-Remaining")).isEqualTo("1");
        }

        @Test
        @DisplayName("USER_IP rule falls back to IP key when no JWT is present")
        void doFilter_userIpRule_noJwt_fallsBackToIp() throws Exception {
            properties = createProperties("/test/**", 1, 60, KeyType.USER_IP);
            rebuildFilter();

            when(jwtAuthUtils.extractJwtFromCookies(any())).thenReturn(null);

            MockHttpServletRequest request1 = buildRequest("/test/a", "10.0.0.1");
            MockHttpServletResponse response1 = buildResponse();
            filter.doFilterInternal(request1, response1, filterChain);

            MockHttpServletRequest request2 = buildRequest("/test/b", "10.0.0.1");
            MockHttpServletResponse response2 = buildResponse();
            filter.doFilterInternal(request2, response2, filterChain);

            verify(filterChain).doFilter(request1, response1);
            verify(responseWriter).write(eq(response2), any());
        }

        @Test
        @DisplayName("USER_IP rule falls back to IP key when JWT is invalid")
        void doFilter_userIpRule_invalidJwt_fallsBackToIp() throws Exception {
            properties = createProperties("/test/**", 1, 60, KeyType.USER_IP);
            rebuildFilter();

            when(jwtAuthUtils.extractJwtFromCookies(any())).thenReturn("bad-token");
            when(jwtAuthUtils.getUserIdFromToken("bad-token"))
                    .thenThrow(new RuntimeException("invalid token"));

            MockHttpServletRequest request1 = buildRequest("/test/a", "10.0.0.1");
            MockHttpServletResponse response1 = buildResponse();
            filter.doFilterInternal(request1, response1, filterChain);

            MockHttpServletRequest request2 = buildRequest("/test/b", "10.0.0.1");
            MockHttpServletResponse response2 = buildResponse();
            filter.doFilterInternal(request2, response2, filterChain);

            verify(filterChain).doFilter(request1, response1);
            verify(responseWriter).write(eq(response2), any());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // evictStaleBuckets()
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("evictStaleBuckets")
    class Eviction {

        @Test
        @DisplayName("Removes buckets older than 1 hour")
        void evictStaleBuckets_removesOldEntries() throws Exception {
            MockHttpServletRequest request = buildRequest("/test/anything", "10.0.0.1");
            MockHttpServletResponse response = buildResponse();
            filter.doFilterInternal(request, response, filterChain);

            Map<String, Object> buckets = getBuckets();
            assertThat(buckets).hasSize(1);

            String key = buckets.keySet().iterator().next();
            Object currentEntry = buckets.get(key);

            Field bucketField = currentEntry.getClass().getDeclaredField("bucket");
            bucketField.setAccessible(true);
            Bucket bucket = (Bucket) bucketField.get(currentEntry);

            long staleTimestamp = System.nanoTime() - (4000L * 1_000_000_000L);
            long staleTimestampMillis = System.currentTimeMillis() - (4000L * 1000L);

            Class<?> entryClass = Class.forName(
                    "com.pkmprojects.shoppiq.filter.RateLimitFilter$BucketEntry");
            Constructor<?> ctor = entryClass.getDeclaredConstructor(Bucket.class, long.class, long.class);
            ctor.setAccessible(true);
            Object staleEntry = ctor.newInstance(bucket, staleTimestamp, staleTimestampMillis);
            buckets.put(key, staleEntry);

            callEvictStaleBuckets();

            assertThat(buckets).isEmpty();
        }

        @Test
        @DisplayName("Keeps buckets that are not yet stale")
        void evictStaleBuckets_keepsFreshEntries() throws Exception {
            MockHttpServletRequest request = buildRequest("/test/anything", "10.0.0.1");
            MockHttpServletResponse response = buildResponse();
            filter.doFilterInternal(request, response, filterChain);

            Map<String, Object> buckets = getBuckets();
            assertThat(buckets).hasSize(1);

            callEvictStaleBuckets();

            assertThat(buckets).hasSize(1);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // formatWaitDuration()
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("formatWaitDuration")
    class FormatWait {

        @Test
        @DisplayName("Formats seconds correctly")
        void formatWaitDuration_seconds() {
            assertThat(RateLimitFilter.formatWaitDuration(0)).isEqualTo("0 seconds");
            assertThat(RateLimitFilter.formatWaitDuration(1)).isEqualTo("1 second");
            assertThat(RateLimitFilter.formatWaitDuration(45)).isEqualTo("45 seconds");
            assertThat(RateLimitFilter.formatWaitDuration(59)).isEqualTo("59 seconds");
        }

        @Test
        @DisplayName("Formats minutes and hours correctly")
        void formatWaitDuration_minutesAndHours() {
            assertThat(RateLimitFilter.formatWaitDuration(60)).isEqualTo("1 minute");
            assertThat(RateLimitFilter.formatWaitDuration(90)).isEqualTo("1 minute");
            assertThat(RateLimitFilter.formatWaitDuration(120)).isEqualTo("2 minutes");
            assertThat(RateLimitFilter.formatWaitDuration(3600)).isEqualTo("1 hour");
            assertThat(RateLimitFilter.formatWaitDuration(7200)).isEqualTo("2 hours");
        }
    }
}
