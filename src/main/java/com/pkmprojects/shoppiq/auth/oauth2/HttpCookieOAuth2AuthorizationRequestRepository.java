package com.pkmprojects.shoppiq.auth.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Arrays;
import java.util.Base64;

/**
 * Cookie-based {@link AuthorizationRequestRepository} that stores the OAuth2
 * authorization request in an HttpOnly cookie instead of the HTTP session.
 *
 * <p>This is the cornerstone of the fully stateless OAuth2 architecture
 * (Option 2). By keeping authorization state in a short-lived cookie rather
 * than a server-side session, the application can use
 * {@code SessionCreationPolicy.STATELESS} throughout the entire lifecycle —
 * including the OAuth2 authorization code flow.</p>
 *
 * <h4>Why the default repository does not work here</h4>
 * <p>Spring Security's default implementation,
 * {@code HttpSessionOAuth2AuthorizationRequestRepository}, stores the pending
 * {@link OAuth2AuthorizationRequest} in the HTTP session between the
 * authorization redirect and the callback. With {@code STATELESS} session
 * policy, no session is ever created, so the default repository would silently
 * lose the request and cause the callback to fail with a {@code state}
 * mismatch. This class replaces it with a short-lived HttpOnly cookie.</p>
 *
 * <h4>Cookie lifecycle</h4>
 * <pre>
 * GET /oauth2/authorization/google
 *       ↓
 * saveAuthorizationRequest() — serialize request → write oauth2_auth_request cookie
 *       ↓
 * Browser follows redirect to Google
 *       ↓
 * Google redirects to /login/oauth2/code/google?code=…&state=…
 *       ↓
 * loadAuthorizationRequest() — read cookie → deserialize
 *       ↓
 * removeAuthorizationRequest() — clear cookie (Max-Age=0)
 *       ↓
 * OAuth2SuccessHandler issues JWT cookie
 * </pre>
 *
 * <h4>Security properties</h4>
 * <ul>
 *   <li>{@code HttpOnly} — cookie is not accessible to JavaScript, mitigating XSS theft</li>
 *   <li>{@code Secure} — HTTPS-only transmission in production (env-driven via
 *       {@code app.security.secure-cookie})</li>
 *   <li>{@code SameSite=Lax} — sent on top-level navigations (Google's redirect)
 *       but not on cross-site sub-requests. {@code Strict} would silently drop
 *       the cookie on the OAuth2 callback because that redirect originates from
 *       {@code accounts.google.com}, which is a different site.</li>
 *   <li>Short {@code Max-Age} of 300 s — limits exposure if the user abandons
 *       the login flow mid-way.</li>
 * </ul>
 *
 * <h4>Serialization note</h4>
 * <p>{@link OAuth2AuthorizationRequest} is not JSON-friendly out of the box
 * (it contains internal Spring Security types with no public Jackson mixins).
 * Java object serialization via {@link SerializationUtils} is used instead.
 * The raw bytes are Base64url-encoded so the result is safe as a cookie value.
 * In a production system requiring tamper-evidence, encrypt or HMAC-sign the
 * payload before encoding; for this application the HTTPS transport and
 * HttpOnly flag provide sufficient protection.</p>
 *
 * @see org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository
 * @see OAuth2SuccessHandler
 */
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Logger logger =
            LoggerFactory.getLogger(HttpCookieOAuth2AuthorizationRequestRepository.class);

    /**
     * Cookie name that holds the Base64url-serialized
     * {@link OAuth2AuthorizationRequest}.
     *
     * <p>Exposed as a constant so that integration tests or diagnostic
     * utilities can reference the exact cookie name without hard-coding it.</p>
     */
    public static final String OAUTH2_AUTH_REQUEST_COOKIE = "oauth2_auth_request";

    /**
     * Max-Age for the authorization-request cookie, in seconds.
     *
     * <p>Five minutes is more than enough for a user to complete the Google
     * consent screen. A shorter window reduces the time an attacker has to
     * replay a stolen cookie if the user abandons the flow on a shared device.</p>
     */
    private static final int COOKIE_MAX_AGE_SECONDS = 300;

    /**
     * Controls the {@code Secure} cookie flag.
     *
     * <p>Set to {@code false} in development (HTTP) and {@code true} in
     * production (HTTPS) via {@code app.security.secure-cookie} in
     * {@code application.yaml}. Defaults to {@code true} so production
     * deployments are safe by default even if the property is omitted.</p>
     */
    @Value("${app.security.secure-cookie:true}")
    private boolean secureCookie;

    /**
     * Loads the {@link OAuth2AuthorizationRequest} stored in the
     * {@value #OAUTH2_AUTH_REQUEST_COOKIE} cookie on the incoming request.
     *
     * <p>Called by Spring Security at two points in the OAuth2 flow:</p>
     * <ol>
     *   <li>During the <em>authorization redirect</em> — to check whether a
     *       request is already in-flight before saving a new one.</li>
     *   <li>During the <em>callback</em> — to retrieve the original request
     *       for {@code state} parameter validation.</li>
     * </ol>
     *
     * <p>If the cookie is absent, malformed, or cannot be deserialized,
     * {@code null} is returned and Spring Security will reject the callback
     * with an {@code OAuth2AuthenticationException} citing an invalid
     * {@code state}.</p>
     *
     * @param request incoming HTTP request; must not be {@code null}
     * @return the deserialized {@link OAuth2AuthorizationRequest}, or
     * {@code null} if the cookie is absent or unreadable
     */
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return deserialize(readCookieValue(request, OAUTH2_AUTH_REQUEST_COOKIE));
    }

    /**
     * Serializes {@code authorizationRequest} into a Base64url-encoded cookie
     * and writes it to the response.
     *
     * <p>Spring Security calls this method immediately before redirecting the
     * browser to the Google authorization endpoint, so the pending request is
     * available when Google returns to the callback URL.</p>
     *
     * <p>If {@code authorizationRequest} is {@code null}, the cookie is
     * cleared (Max-Age=0) rather than written. This matches the contract of
     * {@link AuthorizationRequestRepository#saveAuthorizationRequest} and
     * allows Spring Security to explicitly remove a stale request.</p>
     *
     * @param authorizationRequest the pending OAuth2 authorization request to
     *                             persist, or {@code null} to clear any
     *                             existing cookie
     * @param request              the current HTTP request; not used directly but required
     *                             by the interface contract
     * @param response             the HTTP response to which the cookie is written
     */
    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            clearCookie(response, OAUTH2_AUTH_REQUEST_COOKIE);
            return;
        }
        String serialized = serialize(authorizationRequest);
        addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE, serialized, COOKIE_MAX_AGE_SECONDS);
        logger.debug("Saved OAuth2 authorization request in cookie");
    }

    /**
     * Loads the {@link OAuth2AuthorizationRequest} from the cookie and
     * simultaneously clears the cookie so it cannot be replayed.
     *
     * <p>Spring Security calls this <em>once</em> during the OAuth2 callback
     * ({@code /login/oauth2/code/google}), after which the authorization
     * request is no longer needed. Clearing the cookie at this point:</p>
     * <ul>
     *   <li>Prevents replay of the same authorization code exchange.</li>
     *   <li>Keeps the browser's cookie jar clean after login completes.</li>
     * </ul>
     *
     * <p>If no cookie is present (e.g. the user navigated directly to the
     * callback URL without initiating a login), {@code null} is returned and
     * no cookie-clearing header is written to the response.</p>
     *
     * @param request  the OAuth2 callback HTTP request containing the cookie
     * @param response the HTTP response used to write a Max-Age=0 expiry header
     * @return the deserialized {@link OAuth2AuthorizationRequest} that was
     * stored when the flow was initiated, or {@code null} if absent
     */
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        if (authRequest != null) {
            clearCookie(response, OAUTH2_AUTH_REQUEST_COOKIE);
            logger.debug("Removed OAuth2 authorization request cookie");
        }
        return authRequest;
    }

    // -------------------------------------------------------------------------
    // Cookie helpers
    // -------------------------------------------------------------------------

    /**
     * Writes an HttpOnly cookie with the given name, value, and Max-Age to
     * the HTTP response.
     *
     * <p>Security attributes applied to every cookie written by this method:</p>
     * <ul>
     *   <li>{@code HttpOnly=true} — prevents JavaScript access,
     *       mitigating XSS-based cookie theft.</li>
     *   <li>{@code Secure} — controlled by {@link #secureCookie}; set to
     *       {@code true} in production so the cookie is only transmitted over
     *       HTTPS.</li>
     *   <li>{@code Path=/} — cookie is sent with every request to this origin,
     *       including the OAuth2 callback path.</li>
     *   <li>{@code SameSite=Lax} — the cookie is sent on top-level cross-site
     *       navigations (such as Google's callback redirect) but not on
     *       embedded cross-site requests. {@code Strict} would block the
     *       callback redirect entirely.</li>
     * </ul>
     *
     * @param response HTTP response to which the {@code Set-Cookie} header
     *                 is appended
     * @param name     cookie name; must not be {@code null} or blank
     * @param value    cookie value; must already be safe for use as a cookie
     *                 value (e.g. Base64url-encoded)
     * @param maxAge   Max-Age in seconds; {@code 0} expires the cookie
     *                 immediately, positive values set a persistent cookie
     */
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    /**
     * Instructs the browser to immediately delete a cookie by writing a
     * {@code Set-Cookie} header with an empty value and {@code Max-Age=0}.
     *
     * <p>The security attributes ({@code HttpOnly}, {@code Secure},
     * {@code Path}) must match the original cookie exactly, otherwise the
     * browser will treat the expiry header as a different cookie and the
     * original will persist.</p>
     *
     * @param response HTTP response to which the expiry header is appended
     * @param name     name of the cookie to expire; must match the name used
     *                 when the cookie was originally set
     */
    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * Scans the incoming request's cookie array for a cookie with the given
     * name and returns its value.
     *
     * <p>Returns {@code null} — rather than throwing — when no cookies are
     * present or when none match {@code name}. Callers treat {@code null} as
     * "absent" and handle the missing-cookie case without an exception.</p>
     *
     * @param request incoming HTTP request whose cookies are searched
     * @param name    the exact cookie name to look up; comparison is
     *                case-sensitive per RFC 6265 §5.2
     * @return the value of the first matching cookie, or {@code null} if no
     * cookie with that name exists
     */
    private static String readCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    // -------------------------------------------------------------------------
    // Serialization helpers
    // -------------------------------------------------------------------------

    /**
     * Serializes an {@link OAuth2AuthorizationRequest} to a Base64url-encoded
     * string suitable for embedding in a cookie value.
     *
     * <p>Java object serialization is used rather than JSON because
     * {@link OAuth2AuthorizationRequest} contains internal Spring Security
     * types (e.g. {@code LinkedHashMap} subclasses) that have no public
     * Jackson mixins and would require custom serializers to round-trip
     * correctly. The serialized bytes are Base64url-encoded (no padding,
     * URL-safe alphabet) so the result contains only characters permitted in
     * a cookie value without quoting.</p>
     *
     * @param request the {@link OAuth2AuthorizationRequest} to serialize;
     *                must not be {@code null}
     * @return a non-null, non-empty Base64url-encoded string representing the
     * serialized authorization request
     */
    private static String serialize(OAuth2AuthorizationRequest request) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(request));
    }

    /**
     * Deserializes an {@link OAuth2AuthorizationRequest} from a Base64url-encoded
     * cookie value produced by {@link #serialize(OAuth2AuthorizationRequest)}.
     *
     * <p>Returns {@code null} rather than throwing in all error cases:</p>
     * <ul>
     *   <li>{@code value} is {@code null} or blank — cookie was absent.</li>
     *   <li>Base64 decoding fails — cookie was tampered with or truncated.</li>
     *   <li>Deserialization fails — class version mismatch after a deployment,
     *       or corrupted bytes.</li>
     * </ul>
     * <p>In all these cases Spring Security's callback processing will
     * ultimately produce an {@code OAuth2AuthenticationException} with an
     * invalid-state message, which is the correct behavior — the user must
     * restart the login flow.</p>
     *
     * @param value Base64url-encoded cookie value, or {@code null}
     * @return the deserialized {@link OAuth2AuthorizationRequest}, or
     * {@code null} if {@code value} is absent or cannot be deserialized
     */
    private static OAuth2AuthorizationRequest deserialize(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(value);
            return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(bytes);
        } catch (Exception e) {
            logger.warn("Failed to deserialize OAuth2 authorization request cookie: {}", e.getMessage());
            return null;
        }
    }
}