package com.pkmprojects.shoppiq.auth.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkmprojects.shoppiq.auth.dto.OAuthRegistrationSession;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Cookie-based replacement for the HTTP-session storage of
 * {@link OAuthRegistrationSession}.
 *
 * <h4>Why this exists</h4>
 * <p>With {@code SessionCreationPolicy.STATELESS} the server never creates or
 * reads an {@code HttpSession}. New OAuth2 users who must complete registration
 * still need a short-lived token that survives the round-trip between the
 * OAuth2 callback ({@code OAuth2SuccessHandler}) and the registration endpoint
 * ({@code POST /auth/google/complete-profile}). This service moves that state
 * into an HttpOnly cookie instead of a server-side session.</p>
 *
 * <h4>What the cookie holds</h4>
 * <p>An {@link OAuthRegistrationSession} record — verified Google email,
 * display name, and an {@code authenticatedAt} timestamp — is serialized to
 * JSON and Base64url-encoded. The registration endpoint re-reads this cookie
 * to obtain the verified identity and enforce a timeout window, then clears it
 * once the local account is created.</p>
 *
 * <h4>Cookie lifecycle</h4>
 * <pre>
 * OAuth2SuccessHandler — new user detected
 *       ↓
 * save() — serialize OAuthRegistrationSession → write oauth2_registration cookie
 *       ↓
 * Browser receives 302 redirect to /complete-profile
 *       ↓
 * GET /auth/google/get-profile — read() → return email + name to frontend form
 *       ↓
 * POST /auth/google/complete-profile — read() → validate timeout → create user
 *       ↓
 * clear() — write Max-Age=0 to delete oauth2_registration cookie
 *       ↓
 * JWT cookie issued, user is logged in
 * </pre>
 *
 * <h4>Security</h4>
 * <ul>
 *   <li>{@code HttpOnly} — JavaScript cannot read the cookie, mitigating XSS.</li>
 *   <li>{@code Secure} — HTTPS-only in production (env-driven).</li>
 *   <li>{@code SameSite=Strict} — only sent on same-site requests; the
 *       registration form lives on the same origin, so this does not restrict
 *       legitimate use while blocking CSRF.</li>
 *   <li>Short {@code Max-Age} — controlled by {@code oauth.registration
 *       .timeout-minutes} (default 10 min); the cookie is also re-validated
 *       for expiry server-side in {@code AuthController}.</li>
 * </ul>
 *
 * <h4>Why the cookie content is not sensitive</h4>
 * <p>The cookie carries only the Google-verified email, display name, and a
 * timestamp — information already visible to the user and not a credential.
 * The registration endpoint always re-validates the timeout; if the cookie
 * is replayed after expiry it is rejected. The JWT (the actual credential)
 * is only issued after successful account creation and is written to a
 * separate cookie at that point.</p>
 *
 * @see OAuthRegistrationSession
 * @see com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler
 * @see com.pkmprojects.shoppiq.auth.controller.AuthController
 */
@Component
public class OAuthRegistrationCookieService {

    private static final Logger logger =
            LoggerFactory.getLogger(OAuthRegistrationCookieService.class);

    /**
     * Cookie name that holds the Base64url-encoded JSON representation of
     * {@link OAuthRegistrationSession}.
     *
     * <p>Exposed as a constant so integration tests and diagnostic utilities
     * can reference the exact cookie name without hard-coding it.</p>
     */
    public static final String OAUTH2_REGISTRATION_COOKIE = "oauth2_registration";

    /**
     * Controls the {@code Secure} flag on every cookie written by this service.
     *
     * <p>Driven by {@code app.security.secure-cookie} in
     * {@code application.yaml}. Set to {@code false} for local HTTP development
     * and {@code true} for HTTPS production deployments. Defaults to {@code true}
     * so production is safe even if the property is accidentally omitted.</p>
     */
    @Value("${app.security.secure-cookie:true}")
    private boolean secureCookie;

    /**
     * Max-Age of the registration cookie in minutes, matching the server-side
     * timeout enforced by {@code AuthController}.
     *
     * <p>Driven by {@code oauth.registration.timeout-minutes} in
     * {@code application.yaml} (default 10). The cookie expiry and the
     * server-side timestamp check use the same value so the browser deletes
     * the cookie at approximately the same moment the server would reject it.</p>
     */
    @Value("${oauth.registration.timeout-minutes:10}")
    private int timeoutMinutes;

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * HMAC signing key derived from the JWT secret, used to sign the
     * cookie payload to prevent tampering.
     */
    private SecretKeySpec hmacKey;

    /**
     * Jackson mapper used to serialize/deserialize {@link OAuthRegistrationSession}
     * to/from JSON.
     *
     * <p>JSON is chosen over Java serialization here because
     * {@link OAuthRegistrationSession} is a simple record with only primitive
     * fields and an {@link java.time.Instant}, all of which round-trip cleanly
     * with the application's primary {@link ObjectMapper} (which has
     * {@code JavaTimeModule} registered). This makes the cookie value human-readable
     * when Base64-decoded, which aids debugging.</p>
     */
    private final ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Constructs the service with the application's primary Jackson mapper.
     *
     * @param objectMapper the {@link ObjectMapper} bean provided by
     *                     {@code JacksonConfig}; must not be {@code null}
     */
    public OAuthRegistrationCookieService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        this.hmacKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    /**
     * Serializes {@code session} to JSON, Base64url-encodes the result, and
     * writes it as an HttpOnly cookie to the HTTP response.
     *
     * <p>The cookie's {@code Max-Age} is set to
     * {@code timeoutMinutes × 60} seconds so the browser automatically
     * discards it when the registration window closes, even if the user
     * never submits the form.</p>
     *
     * <p>Serialization failures are logged at {@code ERROR} level and
     * swallowed rather than propagated. If the cookie cannot be written,
     * the registration endpoint will find no cookie and throw an
     * {@link com.pkmprojects.shoppiq.exception.auth.OAuthSessionException},
     * prompting the user to restart the OAuth2 flow — a safe degradation.</p>
     *
     * @param session  the verified Google profile to persist; must not be
     *                 {@code null}
     * @param response outgoing HTTP response to which the
     *                 {@code Set-Cookie} header is appended
     */
    public void save(OAuthRegistrationSession session, HttpServletResponse response) {
        try {
            String json = objectMapper.writeValueAsString(session);
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            String signature = computeHmac(encoded);
            String signedValue = encoded + "." + signature;
            addCookie(response, OAUTH2_REGISTRATION_COOKIE, signedValue, timeoutMinutes * 60);
            logger.debug("Saved OAuth2 registration session in cookie for email: {}", session.email());
        } catch (Exception e) {
            logger.error("Failed to save OAuth2 registration cookie", e);
        }
    }

    /**
     * Reads the {@link OAuthRegistrationSession} from the
     * {@value #OAUTH2_REGISTRATION_COOKIE} cookie on the incoming request.
     *
     * <p>The cookie value is Base64url-decoded and then deserialized from JSON
     * using the injected {@link ObjectMapper}. Returns {@code null} — rather
     * than throwing — when:</p>
     * <ul>
     *   <li>No cookies are present on the request.</li>
     *   <li>The {@value #OAUTH2_REGISTRATION_COOKIE} cookie is absent.</li>
     *   <li>The cookie value is blank (e.g. after a premature clear).</li>
     *   <li>Base64 decoding or JSON deserialization fails (tampered cookie,
     *       encoding mismatch).</li>
     * </ul>
     * <p>Callers in {@code AuthController} check for {@code null} and throw
     * {@link com.pkmprojects.shoppiq.exception.auth.OAuthSessionException}
     * when the session is absent, directing the user to restart the Google
     * login flow.</p>
     *
     * @param request incoming HTTP request whose cookie array is searched;
     *                must not be {@code null}
     * @return the deserialized {@link OAuthRegistrationSession}, or
     * {@code null} if the cookie is absent or unreadable
     */
    public OAuthRegistrationSession read(HttpServletRequest request) {
        String value = readCookieValue(request, OAUTH2_REGISTRATION_COOKIE);
        if (value == null || value.isBlank()) return null;
        try {
            int dotIndex = value.lastIndexOf('.');
            if (dotIndex < 0) {
                logger.warn("OAuth2 registration cookie missing signature");
                return null;
            }
            String payload = value.substring(0, dotIndex);
            String signature = value.substring(dotIndex + 1);

            String expectedSignature = computeHmac(payload);
            if (!MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8))) {
                logger.warn("OAuth2 registration cookie signature mismatch — possible tampering");
                return null;
            }

            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            return objectMapper.readValue(decoded, OAuthRegistrationSession.class);
        } catch (Exception e) {
            logger.warn("Failed to deserialize OAuth2 registration cookie: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Clears the {@value #OAUTH2_REGISTRATION_COOKIE} cookie by writing a
     * {@code Set-Cookie} header with an empty value and {@code Max-Age=0}.
     *
     * <p>Called by {@code AuthController} in all terminal outcomes of the
     * registration flow — successful account creation, session expiry, and
     * duplicate-user errors — so the temporary profile cookie is never left
     * in the browser longer than necessary.</p>
     *
     * <p>The security attributes ({@code HttpOnly}, {@code Secure},
     * {@code Path}) written here must exactly match those set during
     * {@link #save}; a mismatch would cause the browser to treat the expiry
     * header as a different, unrelated cookie and leave the original
     * untouched.</p>
     *
     * @param response outgoing HTTP response to which the expiry
     *                 {@code Set-Cookie} header is appended
     */
    public void clear(HttpServletResponse response) {
        addCookie(response, OAUTH2_REGISTRATION_COOKIE, "", 0);
        logger.debug("Cleared OAuth2 registration cookie");
    }

    /**
     * Writes an HttpOnly cookie with the specified attributes to the HTTP
     * response.
     *
     * <p>All cookies written by this service share the same security
     * attributes:</p>
     * <ul>
     *   <li>{@code HttpOnly=true} — prevents JavaScript access, mitigating
     *       XSS-based theft of the registration profile.</li>
     *   <li>{@code Secure} — controlled by {@link #secureCookie}; {@code true}
     *       in production enforces HTTPS-only transmission.</li>
     *   <li>{@code Path=/} — cookie is sent with every request to this origin,
     *       ensuring it is available on both the {@code /auth/google/get-profile}
     *       and {@code /auth/google/complete-profile} endpoints.</li>
     *   <li>{@code SameSite=Strict} — the registration form lives on the same
     *       origin, so this does not restrict legitimate use while blocking
     *       cross-site request forgery.</li>
     * </ul>
     *
     * <p>Passing {@code maxAge=0} effectively deletes the cookie; this is how
     * {@link #clear(HttpServletResponse)} works internally.</p>
     *
     * @param response HTTP response to which the {@code Set-Cookie} header
     *                 is appended; must not be {@code null}
     * @param name     cookie name; must not be {@code null} or blank
     * @param value    cookie value; should already be safe for use in a cookie
     *                 (e.g. Base64url-encoded or empty string for deletion)
     * @param maxAge   Max-Age in seconds; {@code 0} deletes the cookie,
     *                 positive values create a persistent cookie
     */
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    /**
     * Computes an HMAC-SHA256 signature for the given data and returns it
     * as a Base64url-encoded string without padding.
     *
     * @param data the data to sign
     * @return Base64url-encoded HMAC signature
     */
    private String computeHmac(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(hmacKey);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
    }

    /**
     * Scans the incoming request's cookie array for a cookie with the given
     * name and returns its value.
     *
     * <p>Returns {@code null} — rather than throwing — in the following cases:</p>
     * <ul>
     *   <li>The request carries no cookies at all ({@code getCookies()}
     *       returns {@code null}, which is permitted by the Servlet spec).</li>
     *   <li>No cookie in the array matches {@code name}.</li>
     * </ul>
     * <p>Callers treat {@code null} as "absent" and handle the missing-cookie
     * case explicitly without needing to catch an exception.</p>
     *
     * @param request incoming HTTP request whose cookie array is searched;
     *                must not be {@code null}
     * @param name    the exact cookie name to look up; comparison is
     *                case-sensitive per RFC 6265 §5.2
     * @return the value of the first matching cookie, or {@code null} if no
     * cookie with that name is present
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
}