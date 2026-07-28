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
 * <strong>Spring Boot Concept:</strong> Captures the {@code returnUrl} query parameter on OAuth2 authorization
 * requests and persists it in a cookie so that {@link OAuth2SuccessHandler}
 * can redirect the user back to the original page after authentication.
 *
 * <h3>Spring Security / OAuth2 concepts demonstrated</h3>
 * <ul>
 *   <li><strong>Pre-OAuth2 redirect URL preservation</strong> — when a user
 *       clicks "Login with Google" from a protected page, Spring Security's
 *       {@code OAuth2AuthorizationRequestRedirectFilter} immediately issues
 *       a 302 redirect to Google. This filter runs <em>before</em> that
 *       redirect, capturing the {@code returnUrl} parameter into a cookie
 *       so it survives the OAuth2 round-trip.</li>
 *   <li><strong>Custom filter in the Spring Security chain</strong> — extends
 *       {@link OncePerRequestFilter} and registers itself at the right position
 *       (before the OAuth2 redirect) to intercept the authorization request.</li>
 *   <li><strong>SameSite=Lax for cross-origin redirect</strong> — the cookie
 *       must be {@code SameSite=Lax} (not {@code Strict}) so the browser
 *       sends it when Google redirects back to the application.</li>
 * </ul>
 *
 * <h3>Authentication flow</h3>
 * <pre>
 * User clicks "Login with Google" on /items/123?returnUrl=/items/123
 *       ↓
 * OAuthReturnUrlFilter.doFilterInternal() — reads returnUrl param
 *       ↓
 * Writes oauth_return_url cookie (HttpOnly, SameSite=Lax, 5 min)
 *       ↓
 * Filter chain continues → OAuth2AuthorizationRequestRedirectFilter
 *       ↓
 * Browser redirected to Google consent screen
 *       ↓
 * Google redirects back → /login/oauth2/code/google?code=...&state=...
 *       ↓
 * OAuth2SuccessHandler reads oauth_return_url cookie
 *       ↓
 * Cookie cleared (Max-Age=0), user redirected to original page
 * </pre>
 *
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><strong>Filter-based interception</strong> — uses Spring's
 *       {@link org.springframework.web.filter.OncePerRequestFilter} to
 *       capture a query parameter before the OAuth2 redirect filter
 *       consumes the request.</li>
 *   <li><strong>Cookie as cross-redirect state carrier</strong> — the return
 *       URL is stored in a cookie because the OAuth2 flow involves multiple
 *       redirects (app → Google → app), and cookies automatically travel
 *       with each request to the same origin.</li>
 *   <li><strong>Path-based filtering</strong> — {@link #shouldNotFilter}
 *       ensures this filter only runs on {@code /oauth2/authorization/**}
 *       requests, avoiding unnecessary cookie writes on every request.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
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
