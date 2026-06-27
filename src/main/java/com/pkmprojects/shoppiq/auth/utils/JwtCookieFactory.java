package com.pkmprojects.shoppiq.auth.utils;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory for creating JWT cookies with consistent security attributes.
 *
 * <p>Centralizes cookie configuration so that every JWT cookie — issued at
 * login, OAuth2 authentication, or registration completion — carries
 * identical security flags. The {@code secure} flag is environment-driven
 * via {@code app.security.secure-cookie}, allowing HTTP for development
 * and HTTPS for production.</p>
 *
 * <h4>Cookie attributes</h4>
 * <ul>
 *   <li>{@code HttpOnly} — inaccessible to JavaScript, mitigates XSS</li>
 *   <li>{@code Secure} — HTTPS-only in production</li>
 *   <li>{@code SameSite=Strict} — not sent on cross-site requests, mitigates CSRF</li>
 *   <li>{@code Path=/} — sent with every request to this origin</li>
 *   <li>{@code Max-Age} — controls persistence (session vs. remember-me)</li>
 * </ul>
 *
 * @see JwtAuthenticationUtils
 */
@Component
public class JwtCookieFactory {

    private static final String JWT_COOKIE_NAME = "jwt";

    @Value("${app.security.secure-cookie:true}")
    private boolean secureCookie;

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
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }
}