package com.pkmprojects.shoppiq.auth.handler;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.exception.factory.ProblemDetailFactory;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
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
 * Invoked whenever an authenticated user attempts to access
 * a resource without sufficient permissions.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ShoppiqAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * Response writer used to serialize ProblemDetail responses.
     */
    private final ProblemDetailResponseWriter responseWriter;

    /**
     * Handles authorization failures.
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

        responseWriter.write(response, problemDetail);
    }
}