package com.pkmprojects.shoppiq.util.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Utility component responsible for writing RFC 9457
 * {@link ProblemDetail} responses.
 *
 * <p>
 * This component centralizes the serialization of
 * {@link ProblemDetail} instances into HTTP responses,
 * ensuring a consistent response format across the
 * entire application.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Write {@link ProblemDetail} responses.</li>
 *     <li>Set the HTTP status code.</li>
 *     <li>Set the response content type.</li>
 *     <li>Serialize the response using Jackson.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Contains no business logic.</li>
 *     <li>Can be reused by filters, security handlers and infrastructure components.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ProblemDetailResponseWriter {

    /**
     * Jackson object mapper used for JSON serialization.
     */
    private final ObjectMapper objectMapper;

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