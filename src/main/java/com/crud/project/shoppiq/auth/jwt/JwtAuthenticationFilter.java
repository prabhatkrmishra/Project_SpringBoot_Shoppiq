package com.crud.project.shoppiq.auth.jwt;

import com.crud.project.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.crud.project.shoppiq.auth.utils.JwtCookieFactory;
import com.crud.project.shoppiq.models.User;
import com.crud.project.shoppiq.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * JWT Authentication Filter that processes the JWT cookie on every request.
 *
 * <p>Runs before standard Spring Security filters. Extracts the JWT from the
 * HttpOnly cookie named "jwt", validates it against the database, and builds
 * a complete SecurityContext from the token claims without additional database
 * queries for roles or user details.</p>
 *
 * <p>The filter only sets authentication if the SecurityContext is empty
 * ({@code getAuthentication() == null}), preventing unnecessary replacement
 * of an already-authenticated context.</p>
 *
 * <h4>Stateless request processing</h4>
 * <pre>
 * Incoming HTTP request
 *       ↓
 * Extract JWT from "jwt" cookie
 *       ↓
 * Cookie absent? → continue unauthenticated
 *       ↓
 * Parse claims: userId, username, roles, tokenVersion
 *       ↓
 * Load User from database by userId (single query)
 *       ↓
 * Validate: tokenVersion matches AND user enabled?
 *       ↓
 * Valid → Build UsernamePasswordAuthenticationToken from JWT claims
 *       ↓
 * Set in SecurityContext with authorities from JWT roles
 *       ↓
 * Continue filter chain → Spring Security enforces access rules
 *       ↓
 * Invalid → Clear context → continue unauthenticated
 * </pre>
 *
 * <p>The only database query is loading the user by ID to check token version
 * and enabled status. Roles are taken from the JWT, eliminating per-request
 * role queries.</p>
 *
 * @see JwtAuthenticationUtils
 * @see JwtCookieFactory
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtAuthenticationUtils jwtAuthenticationUtils,
                                   UserRepository userRepository) {
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.userRepository = userRepository;
    }

    /**
     * Processes each request by extracting and validating the JWT cookie,
     * then building a SecurityContext from the token claims.
     *
     * <p>Performs a single database lookup by userId to verify:
     * <ol>
     *   <li>The username in the token matches the database username</li>
     *   <li>The token version matches the current database value</li>
     *   <li>The user account is still enabled</li>
     * </ol>
     *
     * <p>Authorities are built from the JWT roles claim — no additional
     * database queries are needed for authorization decisions.</p>
     *
     * @param request     incoming HTTP request
     * @param response    outgoing HTTP response
     * @param filterChain remaining filters in the chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = jwtAuthenticationUtils.extractJwtFromCookies(request);

            if (token == null) {
                logger.debug("No JWT cookie found in request");
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = jwtAuthenticationUtils.getUserIdFromToken(token);
            String username = jwtAuthenticationUtils.getUsernameFromToken(token);

            if (userId == null || username == null) {
                logger.debug("JWT missing required claims");
                filterChain.doFilter(request, response);
                return;
            }

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                logger.debug("User not found for ID: {}", userId);
                filterChain.doFilter(request, response);
                return;
            }

            User user = userOpt.get();

            if (!jwtAuthenticationUtils.validateToken(token, user)) {
                logger.debug("JWT validation failed for user: {}", username);
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                jwtAuthenticationUtils.getAuthoritiesFromToken(token));
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("JWT authentication set for user: {}", username);
            }

        } catch (Exception e) {
            logger.error("JWT authentication filter error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}