package com.pkmprojects.shoppiq.auth.jwt;

import com.pkmprojects.shoppiq.auth.security.SecurityUser;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.exception.auth.JwtAuthenticationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.exception.factory.ProblemDetailFactory;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import io.jsonwebtoken.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;

/**
 * JWT Authentication Filter — the core of the stateless authentication
 * pipeline. Extends {@link OncePerRequestFilter} to process the JWT cookie
 * on every request exactly once.
 *
 * <h3>Spring Security concepts demonstrated</h3>
 * <ul>
 *   <li><strong>OncePerRequestFilter</strong> — guarantees the filter runs
 *       once per request dispatch, even if the request is forwarded between
 *       servlets. This is the standard base class for JWT authentication
 *       filters in Spring Security applications.</li>
 *   <li><strong>Custom filter chain positioning</strong> — registered via
 *       {@code addFilterBefore(jwtAuthenticationFilter,
 *       UsernamePasswordAuthenticationFilter.class)}, placing it before
 *       Spring Security's form-login filter so that every request is
 *       JWT-checked before any form-based authentication runs.</li>
 *   <li><strong>Stateless SecurityContext population</strong> — builds a
 *       {@link UsernamePasswordAuthenticationToken} with the entity as the
 *       principal and roles from JWT claims, then sets it in
 *       {@link SecurityContextHolder}. No HTTP session is created.</li>
 *   <li><strong>Skip matcher pattern</strong> — endpoints like
 *       {@code /auth/**}, {@code /oauth2/**}, and static resources bypass
 *       JWT validation entirely via {@link #shouldNotFilter} + a
 *       {@link org.springframework.security.web.util.matcher.OrRequestMatcher}.</li>
 *   <li><strong>Token version for forced invalidation</strong> — each JWT
 *       carries the user's {@code tokenVersion}. If the version in the
 *       database is higher (e.g., after password change), the token is
 *       rejected even if the signature is valid.</li>
 * </ul>
 *
 * <h3>Authentication flow per request</h3>
 * <pre>
 * Incoming HTTP request
 *       ↓
 * shouldNotFilter() → /auth/**, /oauth2/**, /error, etc. bypass
 *       ↓
 * Extract JWT from "jwt" cookie
 *       ↓
 * Cookie absent? → continue unauthenticated (filter chain proceeds)
 *       ↓
 * Parse claims: userId, username, roles, tokenVersion
 *       ↓
 * Load User from database by userId (single query)
 *       ↓
 * Validate: tokenVersion matches AND user enabled?
 *       ↓
 * Valid → Build UsernamePasswordAuthenticationToken with SecurityUser as principal
 *       ↓
 * Set in SecurityContext with authorities from JWT roles (no DB query)
 *       ↓
 * Continue filter chain → Spring Security enforces access rules
 *       ↓
 * Invalid JWT → Clear SecurityContext + clear JWT cookie → write RFC 9457 error
 * </pre>
 *
 * <h3>Error handling strategy</h3>
 * <p>
 * Because this filter runs <em>before</em> {@code ExceptionTranslationFilter}
 * in the chain, exceptions thrown here would never reach Spring Security's
 * standard error handling pipeline. The filter therefore catches failures
 * internally and writes RFC 9457 ProblemDetail responses directly via
 * {@link ProblemDetailResponseWriter} — the same infrastructure used by the
 * global exception handler. This pattern mirrors how
 * {@code AbstractAuthenticationProcessingFilter} handles its own failures.
 * </p>
 *
 * <p>The only database query per request is loading the user by ID to verify
 * token version and enabled status. Roles are sourced from the signed JWT,
 * eliminating per-request role queries and keeping authorization fast.</p>
 *
 * @see JwtAuthenticationUtils
 * @see com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory
 * @see com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final JwtCookieFactory jwtCookieFactory;
    private final UserRepository userRepository;
    private final ProblemDetailResponseWriter responseWriter;

    public JwtAuthenticationFilter(JwtAuthenticationUtils jwtAuthenticationUtils,
                                   JwtCookieFactory jwtCookieFactory,
                                   UserRepository userRepository,
                                   ProblemDetailResponseWriter responseWriter) {
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.jwtCookieFactory = jwtCookieFactory;
        this.userRepository = userRepository;
        this.responseWriter = responseWriter;
    }

    /**
     * A composite request matcher that aggregates all public, unauthenticated,
     * and system-level URI endpoints that must completely bypass this security filter.
     *
     * <p>By leveraging {@link PathPatternRequestMatcher} via its static factory
     * method {@link PathPatternRequestMatcher#pathPattern(String)}, this matcher
     * provides highly optimized parsing of URL path expressions natively compatible
     * with Spring MVC routing semantics, outperforming legacy Ant-style matchers.</p>
     *
     * <h2>Bypassed Endpoints</h2>
     * <ul>
     *     <li>{@code /} - Standard application home route</li>
     *     <li>{@code /login} - Standard application login route</li>
     *     <li>{@code /oauth2/**} - Third-party OAuth2 authorization and redirection base pathways</li>
     *     <li>{@code /login/oauth2/**} - OAuth2 processing filters and client landing hooks</li>
     *     <li>{@code /register} - User sign-up and account creation registration endpoint</li>
     *     <li>{@code /error} - Spring Boot global error-dispatch pathway (prevents infinite filter loops on exceptions)</li>
     *     <li>{@code /favicon.ico} - Browser application icon asset request</li>
     *     </ul>
     */
    private static final RequestMatcher SKIP_MATCHER = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern("/error"),
            PathPatternRequestMatcher.pathPattern("/favicon.ico"),
            PathPatternRequestMatcher.pathPattern("/css/**"),
            PathPatternRequestMatcher.pathPattern("/js/**"),
            PathPatternRequestMatcher.pathPattern("/images/**"),
            PathPatternRequestMatcher.pathPattern("/auth/**"),
            PathPatternRequestMatcher.pathPattern("/oauth2/**"),
            PathPatternRequestMatcher.pathPattern("/login/oauth2/**"),
            PathPatternRequestMatcher.pathPattern("/items/all"),
            PathPatternRequestMatcher.pathPattern("/items/*")
    );

    /**
     * Determines whether the incoming HTTP request should completely bypass execution
     * of this filter's JWT validation logic.
     *
     * <p>This method hooks into the lifecycle of {@link OncePerRequestFilter}. If it
     * returns {@code true}, the framework entirely skips {@link #doFilterInternal} for
     * the active request, delegating immediately to the next filter down the line.
     * This prevents unnecessary database lookups, overhead parsing, or early 401/403
     * failures on public-facing infrastructure routes.</p>
     *
     * <h2>Bypass Execution Strategy</h2>
     * <ol>
     *     <li>Extracts the routing context path from the raw HTTP servlet structure.</li>
     *     <li>Evaluates the current request URI and HTTP method payload against the {@link #SKIP_MATCHER}.</li>
     *     <li>If a structural match succeeds, the filter steps aside completely.</li>
     *     <li>If no match succeeds, the request proceeds straight into full JWT validation.</li>
     * </ol>
     *
     * @param request the incoming {@link HttpServletRequest} being evaluated
     * @return {@code true} if the request target matches an entry in {@link #SKIP_MATCHER},
     * meaning it should bypass JWT extraction; {@code false} otherwise
     * @throws ServletException if an error occurs while evaluating the matching rules
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return SKIP_MATCHER.matches(request);
    }

    /**
     * Processes every incoming request exactly once by performing JWT-based
     * authentication before delegating the request further through the Spring
     * Security filter chain.
     *
     * <h2>Authentication Flow</h2>
     * <ol>
     *     <li>Extract the JWT from the HTTP cookie.</li>
     *     <li>If no JWT is present, continue the filter chain without authentication.</li>
     *     <li>Extract mandatory claims (userId and username).</li>
     *     <li>Load the user from the database.</li>
     *     <li>Validate the JWT against the current user state.</li>
     *     <li>Create a {@link UsernamePasswordAuthenticationToken}.</li>
     *     <li>Store the authentication inside the {@link SecurityContextHolder}.</li>
     *     <li>Continue the remaining filter chain.</li>
     * </ol>
     *
     * <h2>JWT Failure Handling</h2>
     * <ul>
     *     <li>If no JWT is supplied, the request continues anonymously.</li>
     *     <li>If the JWT is malformed, invalid, or references an invalid user,
     *     the {@link SecurityContextHolder} is cleared and an RFC 9457
     *     {@code ProblemDetail} response is written directly — see class-level
     *     documentation for why this cannot be delegated further down the chain.</li>
     * </ul>
     *
     * @param request     incoming HTTP request
     * @param response    outgoing HTTP response
     * @param filterChain remaining Spring Security filters
     * @throws ServletException if the filter chain cannot continue
     * @throws IOException      if an I/O error occurs while processing the request
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtAuthenticationUtils.extractJwtFromCookies(request);
        if (token == null) {
            logger.debug("No JWT cookie found.");
            filterChain.doFilter(request, response);
            return;
        }

        try {

            Long userId = jwtAuthenticationUtils.getUserIdFromToken(token);
            String username = jwtAuthenticationUtils.getUsernameFromToken(token);

            if (userId == null || username == null) {
                throw new JwtAuthenticationException(
                        ErrorCode.INVALID_JWT,
                        "JWT token is missing required claims."
                );
            }

            User user = userRepository.findById(userId).orElseThrow(() ->
                    new JwtAuthenticationException(
                            ErrorCode.INVALID_JWT,
                            "JWT references a non-existent user."
                    )
            );

            if (!jwtAuthenticationUtils.validateToken(token, user)) {
                throw new JwtAuthenticationException(
                        ErrorCode.INVALID_JWT,
                        "JWT validation failed."
                );
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                SecurityUser securityUser = new SecurityUser(user);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        securityUser,
                        null,
                        securityUser.getAuthorities()
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Authenticated user '{}'.", username);
            }

            filterChain.doFilter(request, response);

        } catch (JwtException _) {
            SecurityContextHolder.clearContext();
            clearJwtCookie(response);
            if (isBrowserRequest(request)) {
                filterChain.doFilter(request, response);
            } else {
                writeAuthenticationFailure(request, response,
                        new JwtAuthenticationException(ErrorCode.INVALID_JWT, "JWT token is invalid."));
            }
        } catch (JwtAuthenticationException ex) {
            SecurityContextHolder.clearContext();
            clearJwtCookie(response);
            if (isBrowserRequest(request)) {
                filterChain.doFilter(request, response);
            } else {
                writeAuthenticationFailure(request, response, ex);
            }
        }
    }

    /**
     * Writes a JWT authentication failure as an RFC 9457 {@code ProblemDetail}
     * response directly to the client, bypassing the normal Spring MVC
     * exception-resolution path that this filter sits upstream of.
     *
     * @param request   the current HTTP request, used to populate the
     *                  ProblemDetail's {@code instance} field
     * @param response  the HTTP response to write to
     * @param exception the JWT authentication failure
     * @throws IOException if writing the response fails
     */
    private void writeAuthenticationFailure(HttpServletRequest request,
                                            HttpServletResponse response,
                                            JwtAuthenticationException exception) throws IOException {

        logger.debug("JWT authentication failed for [{}]: {}", request.getRequestURI(), exception.getDetail());

        ProblemDetail problemDetail = ProblemDetailFactory.create(
                exception, URI.create(request.getRequestURI()));

        responseWriter.write(response, problemDetail);
    }

    private boolean isBrowserRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }

    private void clearJwtCookie(HttpServletResponse response) {
        response.addCookie(jwtCookieFactory.buildJwtCookie("", 0));
    }
}
