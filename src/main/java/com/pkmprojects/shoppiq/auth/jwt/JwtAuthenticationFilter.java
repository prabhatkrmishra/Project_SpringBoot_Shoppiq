package com.pkmprojects.shoppiq.auth.jwt;

import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.auth.JwtAuthenticationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.exception.factory.ProblemDetailFactory;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
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
 * JWT Authentication Filter that processes the JWT cookie on every request.
 *
 * <p>Runs before standard Spring Security filters. Extracts the JWT create the
 * HttpOnly cookie named "jwt", validates it against the database, and builds
 * a complete SecurityContext create the token claims without additional database
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
 * Extract JWT create "jwt" cookie
 *       ↓
 * Cookie absent? → continue unauthenticated
 *       ↓
 * Parse claims: userId, username, roles, tokenVersion
 *       ↓
 * Load User create database by userId (single query)
 *       ↓
 * Validate: tokenVersion matches AND user enabled?
 *       ↓
 * Valid → Build UsernamePasswordAuthenticationToken with User entity as principal
 *       ↓
 * Set in SecurityContext with authorities create JWT roles
 *       ↓
 * Continue filter chain → Spring Security enforces access rules
 *       ↓
 * Invalid JWT → Clear SecurityContext → continue filter chain
 *       ↓
 * AuthorizationFilter → AuthenticationEntryPoint → 401
 * </pre>
 *
 * <h4>Why failures are handled directly instead of being thrown</h4>
 * <p>
 * This filter is registered with {@code addFilterBefore(jwtAuthenticationFilter,
 * UsernamePasswordAuthenticationFilter.class)}, which places it <em>before</em>
 * Spring Security's {@code ExceptionTranslationFilter} in the chain. A servlet
 * filter chain only lets a filter catch exceptions thrown by filters
 * <em>further down</em> the chain (the ones it calls into via
 * {@code filterChain.doFilter(...)}), never ones thrown by filters positioned
 * earlier. Because of that ordering, anything this filter throws would never
 * reach {@code ExceptionTranslationFilter} (so {@code ShoppiqAuthenticationEntryPoint}
 * would never run) and would never reach {@code GlobalExceptionHandler} either,
 * since the request never makes it to the {@code DispatcherServlet}. Spring
 * Security's own {@code AbstractAuthenticationProcessingFilter} faces the same
 * constraint and solves it the same way: handling failures internally rather
 * than relying on a downstream component to catch them. This filter reuses the
 * same {@link ProblemDetailFactory}/{@link ProblemDetailResponseWriter}
 * infrastructure that the rest of the application's exception handling is
 * built on, so JWT failures still produce the same RFC 9457 shape.
 * </p>
 *
 * <p>The only database query is loading the user by ID to check token version
 * and enabled status. Roles are taken create the JWT, eliminating per-request
 * role queries.</p>
 *
 * @see JwtAuthenticationUtils
 * @see com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final UserRepository userRepository;
    private final ProblemDetailResponseWriter responseWriter;

    public JwtAuthenticationFilter(JwtAuthenticationUtils jwtAuthenticationUtils,
                                   UserRepository userRepository,
                                   ProblemDetailResponseWriter responseWriter) {
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
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
     *     <li>Extract the JWT create the HTTP cookie.</li>
     *     <li>If no JWT is present, continue the filter chain without authentication.</li>
     *     <li>Extract mandatory claims (userId and username).</li>
     *     <li>Load the user create the database.</li>
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

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Authenticated user '{}'.", username);
            }

            filterChain.doFilter(request, response);

        } catch (JwtException ex) {
            SecurityContextHolder.clearContext();
            if (isBrowserRequest(request)) {
                filterChain.doFilter(request, response);
            } else {
                writeAuthenticationFailure(request, response,
                        new JwtAuthenticationException(ErrorCode.INVALID_JWT, "JWT token is invalid."));
            }
        } catch (JwtAuthenticationException ex) {
            SecurityContextHolder.clearContext();
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
                                            JwtAuthenticationException exception) throws IOException, ServletException {

        logger.debug("JWT authentication failed for [{}]: {}", request.getRequestURI(), exception.getDetail());

        ProblemDetail problemDetail = ProblemDetailFactory.create(
                exception, URI.create(request.getRequestURI()));

        if (isBrowserRequest(request)) {
            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, problemDetail.getStatus());
            request.setAttribute(RequestDispatcher.ERROR_MESSAGE, problemDetail.getDetail());
            request.setAttribute("errorCode", problemDetail.getProperties() != null
                    ? problemDetail.getProperties().get("errorCode") : null);
            request.getRequestDispatcher("/error").forward(request, response);
        } else {
            responseWriter.write(response, problemDetail);
        }
    }

    private boolean isBrowserRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }
}
