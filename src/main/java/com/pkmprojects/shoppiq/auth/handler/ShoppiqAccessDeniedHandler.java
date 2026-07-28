package com.pkmprojects.shoppiq.auth.handler;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.exception.factory.ProblemDetailFactory;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

/**
 * <strong>Spring Boot Concept:</strong> Spring Security {@link AccessDeniedHandler} — converts authorization
 * (403) failures into RFC 9457 {@code ProblemDetail} responses.
 *
 * <h3>Spring Security concepts demonstrated</h3>
 * <ul>
 *   <li><strong>AccessDeniedHandler contract</strong> — Spring Security invokes
 *       this handler when a client <em>is</em> authenticated but lacks the
 *       required authority for a resource. This distinguishes it from
 *       {@link com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint},
 *       which handles the unauthenticated (401) case.</li>
 *   <li><strong>Authorization vs. Authentication</strong> — 401 means "who are
 *       you?" (missing/invalid credentials); 403 means "you are known but not
 *       allowed" (insufficient roles). This class handles the latter.</li>
 *   <li><strong>Role-based access control (RBAC)</strong> — the filter chain
 *       that leads here was configured with rules like
 *       {@code .hasRole("ADMIN")} or {@code .hasAuthority("ROLE_CUSTOMER")}
 *       in the security configuration.</li>
 * </ul>
 *
 * <h3>Authentication flow</h3>
 * <pre>
 * Authenticated request → AuthorizationFilter checks authorities
 *       ↓
 * Insufficient permissions → AccessDeniedException thrown
 *       ↓
 * ExceptionTranslationFilter catches it
 *       ↓
 * Calls AccessDeniedHandler.handle()
 *       ↓
 * ProblemDetailFactory creates RFC 9457 error → forwarded to /error
 * </pre>
 *
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><strong>Strategy pattern</strong> — {@link AccessDeniedHandler} is a
 *       strategy interface; this class provides the Shoppiq-specific implementation.</li>
 *   <li><strong>Content-negotiated error response</strong> — API clients
 *       ({@code Accept: application/json}) receive a structured RFC 9457 JSON
 *       response directly, while browser requests are forwarded to the error page
 *       (BUG-009).</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ShoppiqAccessDeniedHandler implements AccessDeniedHandler {

    private final ProblemDetailResponseWriter responseWriter;

    /**
     * Handles the access denied exception by returning
     * an RFC 9457 ProblemDetail response.
     *
     * @param request   HTTP request
     * @param response  HTTP response
     * @param exception access denied exception
     * @throws IOException      if writing the response fails
     * @throws ServletException if servlet processing fails
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
            throws IOException, ServletException {

        ProblemDetail problemDetail =
                ProblemDetailFactory.create(
                        HttpStatus.FORBIDDEN,
                        ErrorCode.ACCESS_DENIED.getDefaultMessage(),
                        ErrorCode.ACCESS_DENIED,
                        URI.create(request.getRequestURI())
                );

        if (isBrowserRequest(request)) {
            forwardToErrorPage(request, response, problemDetail);
        } else {
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

    /**
     * Forwards the request to the /error page with error attributes set.
     */
    private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response,
                                     ProblemDetail problemDetail) throws IOException, ServletException {
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, problemDetail.getStatus());
        request.setAttribute(RequestDispatcher.ERROR_MESSAGE, problemDetail.getDetail());
        request.setAttribute("errorCode", problemDetail.getProperties() != null
                ? problemDetail.getProperties().get("errorCode") : null);
        request.getRequestDispatcher("/error").forward(request, response);
    }

}
