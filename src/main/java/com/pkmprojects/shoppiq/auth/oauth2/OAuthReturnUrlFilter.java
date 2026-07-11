package com.pkmprojects.shoppiq.auth.oauth2;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Captures the {@code returnUrl} query parameter on OAuth2 authorization
 * requests and persists it in a cookie so that {@link OAuth2SuccessHandler}
 * can redirect the user back to the original page after authentication.
 *
 * <p>This filter runs <em>before</em> Spring Security's
 * {@code OAuth2AuthorizationRequestRedirectFilter}, setting the cookie in
 * the HTTP response headers before any redirect to the identity provider
 * occurs. This guarantees the cookie is received by the browser regardless
 * of the OAuth2 redirect chain.</p>
 */
@Component
public class OAuthReturnUrlFilter extends OncePerRequestFilter {

    static final String COOKIE_NAME = "oauth_return_url";
    private static final int COOKIE_MAX_AGE = 300; // 5 minutes

    private final boolean secureCookie;

    public OAuthReturnUrlFilter(@Value("${app.security.secure-cookie:true}") boolean secureCookie) {
        this.secureCookie = secureCookie;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String returnUrl = request.getParameter("returnUrl");
        if (returnUrl != null && returnUrl.startsWith("/") && !returnUrl.startsWith("//")) {
            Cookie cookie = new Cookie(COOKIE_NAME, returnUrl);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(secureCookie);
            cookie.setMaxAge(COOKIE_MAX_AGE);
            cookie.setAttribute("SameSite", "Lax");
            response.addCookie(cookie);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.startsWith("/oauth2/authorization");
    }
}
