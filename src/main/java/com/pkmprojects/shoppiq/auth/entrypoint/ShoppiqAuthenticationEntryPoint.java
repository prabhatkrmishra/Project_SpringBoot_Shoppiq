package com.pkmprojects.shoppiq.auth.entrypoint;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.exception.factory.ProblemDetailFactory;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import com.pkmprojects.shoppiq.util.http.RequestUtils;
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
 * Converts authentication failures into RFC 9457 ProblemDetail responses.
 *
 * <p>Routes API clients to structured JSON errors and browser requests to
 * the login page with the original destination preserved.</p>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.exception.factory.ProblemDetailFactory
 * @see com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter
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
            String query = request.getQueryString();
            String returnUrl = query != null ? uri + "?" + query : uri;
            response.sendRedirect("/login?returnUrl=" + java.net.URLEncoder.encode(returnUrl, java.nio.charset.StandardCharsets.UTF_8));
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
        return RequestUtils.isBrowserRequest(request);
    }

}
