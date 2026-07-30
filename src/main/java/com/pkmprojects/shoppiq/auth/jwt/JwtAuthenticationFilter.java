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
import com.pkmprojects.shoppiq.util.http.RequestUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;

/**
 * Core JWT authentication filter for stateless request processing.
 *
 * <p>This filter intercepts every incoming request and extracts the JWT
 * token from the HttpOnly cookie. It validates the token's signature,
 * expiration, and token version, then populates the Spring Security
 * {@link org.springframework.security.core.context.SecurityContext} with
 * the user's principal and roles. Public endpoints bypass validation via
 * a skip matcher to avoid unnecessary token processing.</p>
 *
 * <p>The filter is positioned before the
 * {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter}
 * in the security filter chain. It handles token expiration by returning
 * a 401 Problem Detail response with the {@link ErrorCode#JWT_EXPIRED}
 * code, and invalid tokens with {@link ErrorCode#INVALID_JWT}. The filter
 * also supports token version invalidation for forced logout scenarios.</p>
 *
 * @author prabhatkrmishra
 * @see JwtAuthenticationUtils
 * @see JwtCookieFactory
 * @since 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    /**
     * A composite request matcher that aggregates all public, unauthenticated,
     * and system-level URI endpoints that must completely bypass this security filter.
     *
     * <p>By leveraging {@link PathPatternRequestMatcher} via its static factory
     * method {@link PathPatternRequestMatcher#pathPattern(String)}, this matcher
     * provides highly optimized parsing of URL path expressions natively compatible
     * with Spring MVC routing semantics, outperforming legacy Ant-style matchers.</p>
     *
     * <p>Bypassed endpoints include: {@code /error} (Spring Boot global error-dispatch
     * pathway preventing infinite filter loops), {@code /favicon.ico} (browser
     * application icon asset request), {@code /css/**} and {@code /js/**} and
     * {@code /images/**} (static resources), {@code /auth/**} (authentication
     * routes), {@code /oauth2/**} and {@code /login/oauth2/**} (OAuth2
     * authorization and processing filters), {@code /items/all} and
     * {@code /items/*} (public item browsing endpoints).</p>
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
     * Determines whether the incoming HTTP request should completely bypass execution
     * of this filter's JWT validation logic.
     *
     * <p>This method hooks into the lifecycle of {@link OncePerRequestFilter}. If it
     * returns {@code true}, the framework entirely skips {@link #doFilterInternal} for
     * the active request, delegating immediately to the next filter down the line.
     * This prevents unnecessary database lookups, overhead parsing, or early 401/403
     * failures on public-facing infrastructure routes.</p>
     *
     * <p>The bypass execution strategy works as follows: the routing context path
     * is extracted from the raw HTTP servlet structure, the current request URI and
     * HTTP method are evaluated against the {@link #SKIP_MATCHER}, and if a match
     * succeeds the filter steps aside completely. If no match succeeds, the request
     * proceeds straight into full JWT validation.</p>
     *
     * @param request the incoming {@link HttpServletRequest} being evaluated
     * @return {@code true} if the request target matches an entry in
     * {@link #SKIP_MATCHER}, meaning it should bypass JWT extraction;
     * {@code false} otherwise
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
     * <p>The authentication flow proceeds as follows: the JWT is extracted from
     * the HTTP cookie. If no JWT is present, the filter chain continues without
     * authentication. If a token is present, mandatory claims (userId and
     * username) are extracted, the user is loaded from the database, and the
     * JWT is validated against the current user state. A
     * {@link UsernamePasswordAuthenticationToken} is created and stored inside
     * the {@link SecurityContextHolder} before continuing the remaining filter
     * chain.</p>
     *
     * <p>If no JWT is supplied, the request continues anonymously. If the JWT
     * is malformed, invalid, or references an invalid user, the
     * {@link SecurityContextHolder} is cleared and an RFC 9457
     * {@code ProblemDetail} response is written directly for API requests.
     * Browser requests are forwarded to the next filter for error-page
     * rendering.</p>
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
            Object[] userIdAndUsername = jwtAuthenticationUtils.extractUserIdAndUsername(token);
            Long userId = (Long) userIdAndUsername[0];
            String username = (String) userIdAndUsername[1];

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
        return RequestUtils.isBrowserRequest(request);
    }

    private void clearJwtCookie(HttpServletResponse response) {
        response.addCookie(jwtCookieFactory.buildJwtCookie("", 0));
    }
}
