package com.pkmprojects.shoppiq.auth.entrypoint;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.exception.factory.ProblemDetailFactory;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

/**
 * <strong>Spring Boot Concept:</strong> Spring Security {@link AuthenticationEntryPoint} — converts authentication
 * failures into RFC 9457 {@code ProblemDetail} responses.
 *
 * <h3>Spring Security concepts demonstrated</h3>
 * <ul>
 *   <li><strong>AuthenticationEntryPoint contract</strong> — Spring Security
 *       invokes this class when an unauthenticated client (no valid JWT) tries
 *       to access a protected endpoint. It is registered in the security filter
 *       chain and triggered by {@code ExceptionTranslationFilter}.</li>
 *   <li><strong>Content-negotiated error response</strong> — API clients
 *       ({@code Accept: application/json}) receive a structured RFC 9457 JSON
 *       response, while browser requests are redirected to the login page with
 *       a {@code returnUrl} parameter preserving the original destination.</li>
 *   <li><strong>RFC 9457 ProblemDetail</strong> — a standardized error format
 *       ({@code type}, {@code title}, {@code status}, {@code detail},
 *       {@code instance}, custom properties) that supersedes ad-hoc error bodies.
 *       Spring Boot 3+ natively supports {@code ProblemDetail} via
 *       {@code ErrorController}.</li>
 * </ul>
 *
 * <h3>Authentication flow</h3>
 * <pre>
 * Unauthenticated request → Filter chain → AuthorizationFilter rejects
 *       ↓
 * ExceptionTranslationFilter catches AccessDeniedException/AuthenticationException
 *       ↓
 * Calls AuthenticationEntryPoint.commence()
 *       ↓
 * ┌─ API request → ProblemDetailFactory + ProblemDetailResponseWriter → JSON
 * └─ Browser    → 302 redirect to /login?returnUrl=...
 * </pre>
 *
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><strong>Strategy pattern</strong> — {@link AuthenticationEntryPoint} is a
 *       strategy interface; this class provides the Shoppiq-specific implementation.</li>
 *   <li><strong>Factory delegation</strong> — error details are built by
 *       {@link com.pkmprojects.shoppiq.exception.factory.ProblemDetailFactory}
 *       and written by {@link com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter},
 *       keeping this class focused on routing logic.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ShoppiqAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * ProblemDetail factory.
     */
    private final ProblemDetailResponseWriter responseWriter;

    /**
     * Commences the authentication process by returning
     * an RFC 9457 ProblemDetail response.
     *
     * @param request   HTTP request
     * @param response  HTTP response
     * @param exception authentication exception
     * @throws IOException      if writing the response fails
     * @throws ServletException if servlet processing fails
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {

        if (isBrowserRequest(request)) {
            String uri = request.getRequestURI();
            response.sendRedirect("/login?returnUrl=" + java.net.URLEncoder.encode(uri, java.nio.charset.StandardCharsets.UTF_8));
        } else {
            ProblemDetail problemDetail =
                    ProblemDetailFactory.create(
                            HttpStatus.UNAUTHORIZED,
                            exception.getMessage(),
                            ErrorCode.UNAUTHORIZED,
                            URI.create(request.getRequestURI())
                    );
            responseWriter.write(response, problemDetail);
        }
    }

    /**
     * Checks if the request is from a browser (Accept header contains text/html).
     */
    private boolean isBrowserRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }

}
