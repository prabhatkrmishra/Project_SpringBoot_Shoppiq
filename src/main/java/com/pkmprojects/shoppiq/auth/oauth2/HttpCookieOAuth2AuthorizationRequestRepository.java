package com.pkmprojects.shoppiq.auth.oauth2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
 * loadAuthorizationRequest() — read cookie → verify HMAC → deserialize
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
 *   <li>HMAC-SHA256 signed payload — prevents tampering and RCE via Java deserialization gadget chains.</li>
 * </ul>
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
     * Cookie name that holds the Base64url-encoded, HMAC-signed JSON
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
     * HMAC algorithm used for signing the cookie payload.
     */
    private static final String HMAC_ALGORITHM = "HmacSHA256";

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
     * HMAC signing key derived from the application's JWT secret.
     * Injected via {@code jwt.secret} property.
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private byte[] hmacKey;

    @jakarta.annotation.PostConstruct
    private void init() {
        this.hmacKey = jwtSecret.getBytes(StandardCharsets.UTF_8);
    }

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
     * <p>If the cookie is absent, malformed, HMAC verification fails, or
     * deserialization fails, {@code null} is returned and Spring Security will
     * reject the callback with an {@code OAuth2AuthenticationException} citing
     * an invalid {@code state}.</p>
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
     * Serializes {@code authorizationRequest} into a Base64url-encoded,
     * HMAC-signed JSON cookie and writes it to the response.
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
        cookie.setAttribute("SameSite", "Lax");
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
    // Secure serialization helpers (JSON + HMAC)
    // -------------------------------------------------------------------------

    /**
     * Serializes an {@link OAuth2AuthorizationRequest} to a Base64url-encoded,
     * HMAC-signed JSON string suitable for embedding in a cookie value.
     *
     * <p>Only the fields required to reconstruct the authorization request for
     * the callback are stored. The payload is signed with HMAC-SHA256 using
     * the application's JWT secret, providing integrity and authenticity.
     * This replaces the previous Java serialization approach which was
     * vulnerable to RCE via gadget chains.</p>
     *
     * @param request the {@link OAuth2AuthorizationRequest} to serialize;
     *                must not be {@code null}
     * @return a non-null, non-empty Base64url-encoded string: {@code payload.signature}
     */
    private String serialize(OAuth2AuthorizationRequest request) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("authorizationUri", request.getAuthorizationUri());
            payload.put("authorizationRequestUri", request.getAuthorizationRequestUri());
            payload.put("redirectUri", request.getRedirectUri());
            payload.put("scopes", request.getScopes());
            payload.put("state", request.getState());
            payload.put("additionalParameters", request.getAdditionalParameters());
            payload.put("attributes", request.getAttributes());
            payload.put("clientId", request.getClientId());
            payload.put("grantType", request.getGrantType() != null ? request.getGrantType().getValue() : null);

            String json = objectMapper.writeValueAsString(payload);
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            String signature = sign(encodedPayload);
            return encodedPayload + "." + signature;
        } catch (Exception e) {
            logger.error("Failed to serialize OAuth2 authorization request", e);
            throw new IllegalStateException("Cannot serialize authorization request", e);
        }
    }

    /**
     * Deserializes an {@link OAuth2AuthorizationRequest} from a Base64url-encoded,
     * HMAC-signed cookie value produced by {@link #serialize(OAuth2AuthorizationRequest)}.
     *
     * <p>Returns {@code null} rather than throwing in all error cases:</p>
     * <ul>
     *   <li>{@code value} is {@code null} or blank — cookie was absent.</li>
     *   <li>Format is invalid (missing dot separator) — cookie was tampered with.</li>
     *   <li>HMAC verification fails — cookie was tampered with or forged.</li>
     *   <li>Base64 decoding fails — cookie was corrupted.</li>
     *   <li>JSON parsing fails — cookie was corrupted or from an incompatible version.</li>
     * </ul>
     * <p>In all these cases Spring Security's callback processing will
     * ultimately produce an {@code OAuth2AuthenticationException} with an
     * invalid-state message, which is the correct behavior — the user must
     * restart the login flow.</p>
     *
     * @param value Base64url-encoded cookie value with HMAC signature ({@code payload.signature}), or {@code null}
     * @return the deserialized {@link OAuth2AuthorizationRequest}, or
     * {@code null} if {@code value} is absent or cannot be verified/deserialized
     */
    private OAuth2AuthorizationRequest deserialize(String value) {
        if (value == null || value.isBlank()) return null;

        int dotIndex = value.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == value.length() - 1) {
            logger.warn("Invalid OAuth2 authorization request cookie format");
            return null;
        }

        String encodedPayload = value.substring(0, dotIndex);
        String signature = value.substring(dotIndex + 1);

        if (!verifySignature(encodedPayload, signature)) {
            logger.warn("OAuth2 authorization request cookie HMAC verification failed");
            return null;
        }

        try {
            byte[] jsonBytes = Base64.getUrlDecoder().decode(encodedPayload);
            String json = new String(jsonBytes, StandardCharsets.UTF_8);

            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {
            };
            Map<String, Object> payload = objectMapper.readValue(json, typeRef);

            String authorizationRequestUri = (String) payload.get("authorizationRequestUri");
            String authorizationUri = (String) payload.get("authorizationUri");
            String redirectUri = (String) payload.get("redirectUri");
            @SuppressWarnings("unchecked")
            Set<String> scopes = ((java.util.List<String>) payload.get("scopes")).stream().collect(Collectors.toSet());
            String state = (String) payload.get("state");
            @SuppressWarnings("unchecked")
            Map<String, Object> additionalParameters = (Map<String, Object>) payload.get("additionalParameters");
            @SuppressWarnings("unchecked")
            Map<String, Object> attributes = (Map<String, Object>) payload.get("attributes");
            String clientId = (String) payload.get("clientId");
            String grantType = (String) payload.get("grantType");

            OAuth2AuthorizationRequest.Builder builder =
                    OAuth2AuthorizationRequest.authorizationCode();

            String resolvedAuthorizationUri = authorizationUri;
            if (resolvedAuthorizationUri == null && authorizationRequestUri != null) {
                int q = authorizationRequestUri.indexOf('?');
                resolvedAuthorizationUri = q > 0
                        ? authorizationRequestUri.substring(0, q)
                        : authorizationRequestUri;
            }

            if (resolvedAuthorizationUri == null) {
                logger.warn("Cannot reconstruct OAuth2 authorization request: no authorization URI in cookie");
                return null;
            }

            builder.authorizationUri(resolvedAuthorizationUri);
            if (authorizationRequestUri != null) {
                builder.authorizationRequestUri(authorizationRequestUri);
            }

            return builder
                    .redirectUri(redirectUri)
                    .scopes(scopes)
                    .state(state)
                    .additionalParameters(additionalParameters != null ? additionalParameters : java.util.Map.of())
                    .attributes(attributes != null ? attributes : java.util.Map.of())
                    .clientId(clientId)
                    .build();
        } catch (Exception e) {
            logger.warn("Failed to deserialize OAuth2 authorization request cookie: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Computes HMAC-SHA256 signature for the given payload.
     *
     * @param payload Base64url-encoded JSON payload
     * @return Base64url-encoded HMAC signature (no padding)
     */
    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(hmacKey, HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC initialization failed", e);
        }
    }

    /**
     * Verifies HMAC-SHA256 signature for the given payload.
     *
     * @param payload   Base64url-encoded JSON payload
     * @param signature Base64url-encoded HMAC signature
     * @return {@code true} if signature is valid
     */
    private boolean verifySignature(String payload, String signature) {
        try {
            String expected = sign(payload);
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
}