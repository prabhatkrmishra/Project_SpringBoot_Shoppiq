package com.pkmprojects.shoppiq.auth.utils;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory for creating JWT cookies with consistent security attributes.
 *
 * <p>This component creates HttpOnly, SameSite=Lax cookies for JWT transport
 * with configurable Secure flag. It supports three cookie types: session
 * cookies (browser-close expiration), persistent cookies (fixed max-age),
 * and expiring cookies (for OAuth2 state). The Secure flag is configurable
 * through the {@code app.security.secure-cookie} property to allow
 * HTTP in development while enforcing HTTPS in production.</p>
 *
 * <p>The cookie factory is used by the authentication service for login/logout,
 * by the OAuth2 success handler for setting tokens after social login, and
 * by the refresh endpoint for issuing new tokens. All cookies are created
 * through this factory to ensure consistent security attributes across the
 * application.</p>
 *
 * @author prabhatkrmishra
 * @see JwtAuthenticationUtils
 * @see com.pkmprojects.shoppiq.auth.service.AuthService
 * @see com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler
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
