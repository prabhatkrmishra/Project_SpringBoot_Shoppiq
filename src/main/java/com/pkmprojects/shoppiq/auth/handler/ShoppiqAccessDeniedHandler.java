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
 * Spring Security access denied handler responsible for converting
 * authorization failures into RFC 9457 compliant responses.
 *
 * <p>
 * Invoked whenever an authenticated but unauthorized client attempts
 * to access a protected resource.
 * </p>
 *
 * <p>
 * For API requests (Accept: application/json), returns a JSON ProblemDetail response.
 * For browser requests (Accept: text/html), forwards to the /error page.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ShoppiqAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * ProblemDetail writer.
     */
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
