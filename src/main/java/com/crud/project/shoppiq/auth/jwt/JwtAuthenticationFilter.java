package com.crud.project.shoppiq.auth.jwt;

import com.crud.project.shoppiq.auth.utils.JwtAuthenticationUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * JWT Authentication Filter.
 * Intercepts every HTTP request, extracts the JWT from the HttpOnly cookie
 * named "jwt", validates it, and sets authentication in the SecurityContext
 * if valid. Runs before standard Spring Security filters.
 *
 * <p>Cookies are used instead of the Authorization header so the token is
 * never accessible to JavaScript (HttpOnly), reducing XSS exposure. The
 * SameSite=Strict attribute set by the login endpoint additionally mitigates
 * CSRF for modern browsers.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Name of the HttpOnly cookie that carries the JWT.
     */
    private static final String JWT_COOKIE_NAME = "jwt";

    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtAuthenticationUtils jwtAuthenticationUtils,
                                   UserDetailsService userDetailsService) {
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Main filter method executed once per request.
     *
     * <p>Reads the {@code jwt} cookie from the incoming request, validates the
     * token, loads user details, and populates the {@link SecurityContextHolder}
     * so that Spring Security can enforce role-based access control
     * ({@code @PreAuthorize}, {@code hasRole}, etc.) for the remainder of the
     * request lifecycle.</p>
     *
     * <p>If the cookie is absent, malformed, or the token fails validation the
     * filter clears the SecurityContext and lets the request continue
     * unauthenticated — downstream security rules will reject it with 401/403
     * as appropriate.</p>
     *
     * <p>Called automatically by the Spring Security filter chain.</p>
     *
     * @param request     incoming HTTP request
     * @param response    outgoing HTTP response
     * @param filterChain remaining filters in the chain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractJwtFromCookies(request);

            if (token != null) {
                String username = jwtAuthenticationUtils.getUsernameFromToken(token);

                if (username != null) {

                    /*
                     * Load the application's UserDetails from the database.
                     *
                     * Although OAuth2 login initially authenticates the user using
                     * Spring Security's OAuth2AuthenticationToken, authorization
                     * throughout the application is based on our own User entity
                     * and roles stored in the database (ROLE_CUSTOMER, ROLE_ADMIN).
                     *
                     * Therefore every request backed by a valid JWT is mapped back
                     * to the application's UserDetails before authorization checks.
                     */
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtAuthenticationUtils.validateToken(token, userDetails)) {
                        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

                        /*
                         * Google OAuth2 login populates the SecurityContext with an
                         * OAuth2AuthenticationToken containing authorities such as:
                         *
                         *   OIDC_USER
                         *   SCOPE_openid
                         *   SCOPE_profile
                         *
                         * These authorities do not satisfy application rules such as:
                         *
                         *   hasRole("CUSTOMER")
                         *   hasRole("ADMIN")
                         *
                         * Therefore we replace the OAuth2 authentication with a
                         * UsernamePasswordAuthenticationToken backed by our own
                         * UserDetails implementation so that application roles are
                         * consistently available regardless of whether the user
                         * logged in via username/password or Google OAuth2.
                         *
                         * We only replace the authentication when it is not already
                         * a UsernamePasswordAuthenticationToken to avoid rebuilding
                         * the same authentication object on every request.
                         */
                        if (!(currentAuth instanceof UsernamePasswordAuthenticationToken)) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                }
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Scans the request's cookie array for the {@code jwt} cookie and returns
     * its value, or {@code null} if the cookie is absent or the request carries
     * no cookies at all.
     *
     * @param request incoming HTTP request
     * @return raw JWT string, or {@code null} if not found
     */
    private String extractJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(c -> JWT_COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}