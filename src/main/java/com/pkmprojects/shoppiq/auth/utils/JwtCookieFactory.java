package com.pkmprojects.shoppiq.auth.utils;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory for creating JWT cookies with consistent security attributes.
 *
 * <h3>Spring Security / cookie concepts demonstrated</h3>
 * <ul>
 *   <li><strong>HttpOnly cookie for JWT transport</strong> — the JWT is
 *       delivered exclusively as an HttpOnly cookie (never in a response body
 *       or {@code Authorization} header). This prevents JavaScript from
 *       reading the token, mitigating XSS-based credential theft.</li>
 *   <li><strong>SameSite=Lax CSRF protection</strong> — the {@code SameSite=Lax}
 *       attribute tells the browser to send the cookie only on same-site
 *       top-level navigations, not on cross-site requests. This provides
 *       built-in CSRF protection without requiring a separate CSRF token.</li>
 *   <li><strong>Environment-driven Secure flag</strong> — the {@code Secure}
 *       flag is controlled by {@code app.security.secure-cookie}, enabling
 *       HTTP for local development and HTTPS for production without code changes.</li>
 *   <li><strong>Max-Age for session vs. persistent cookies</strong> —
 *       {@code Max-Age=-1} creates a session cookie (deleted when browser
 *       closes); {@code Max-Age=0} expires it immediately (logout);
 *       positive values create persistent cookies (remember-me).</li>
 * </ul>
 *
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><strong>Factory pattern</strong> — a single place that creates
 *       consistently-configured cookies. Every JWT cookie in the application
 *       goes through this factory, ensuring identical security attributes.</li>
 *   <li><strong>Centralized configuration</strong> — the {@code Secure} flag
 *       is injected once and applied to all cookies, avoiding scattered
 *       conditionals throughout the codebase.</li>
 *   <li><strong>Simple API</strong> — a single {@link #buildJwtCookie} method
 *       accepts the token value and Max-Age, hiding all cookie attribute
 *       complexity from callers.</li>
 * </ul>
 *
 * @see JwtAuthenticationUtils
 * @see com.pkmprojects.shoppiq.auth.service.AuthService
 * @see com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Component
public class JwtCookieFactory {

    private static final String JWT_COOKIE_NAME = "jwt";

    private final boolean secureCookie;

    public JwtCookieFactory(@Value("${app.security.secure-cookie:true}") boolean secureCookie) {
        this.secureCookie = secureCookie;
    }

    /**
     * Builds a JWT cookie with security attributes.
     *
     * @param value  JWT token string (login/registration) or empty string (logout)
     * @param maxAge {@code -1} = session cookie, {@code 0} = immediate expiration,
     *               {@code > 0} = persistent cookie in seconds
     * @return fully configured Cookie
     */
    public Cookie buildJwtCookie(String value, int maxAge) {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}
