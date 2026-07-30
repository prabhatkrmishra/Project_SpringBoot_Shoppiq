package com.pkmprojects.shoppiq.util.http;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Utility component for writing RFC 9457 {@link ProblemDetail} responses.
 *
 * <p>Centralizes serialization of {@link ProblemDetail} instances into
 * HTTP responses, ensuring consistent error response format across the
 * application. This component is used by security handlers, filters, and
 * the global exception handler to write structured error responses.</p>
 *
 * <p>The writer sets the appropriate HTTP status code, content type
 * ({@code application/problem+json}), and character encoding before
 * serializing the Problem Detail to the response output stream. All
 * error responses produced by this writer conform to RFC 9457.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ProblemDetailResponseWriter {

    /**
     * Jackson 3 JSON mapper used for serialization.
     */
    private final JsonMapper objectMapper;

    /**
     * Writes the supplied {@link ProblemDetail} to the HTTP response.
     *
     * @param response      HTTP response
     * @param problemDetail RFC 9457 ProblemDetail
     * @throws IOException if serialization fails
     */
    public void write(HttpServletResponse response, ProblemDetail problemDetail)
            throws IOException {

        response.setStatus(problemDetail.getStatus());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(
                MediaType.APPLICATION_PROBLEM_JSON_VALUE
        );

        objectMapper.writeValue(response.getOutputStream(), problemDetail);
    }
}
