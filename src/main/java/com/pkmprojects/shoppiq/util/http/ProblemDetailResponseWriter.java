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
 * <strong>Spring Boot Concept:</strong> Utility component responsible for writing RFC 9457
 * {@link ProblemDetail} responses.
 *
 * <p>
 * This component centralizes the serialization of
 * {@link ProblemDetail} instances into HTTP responses,
 * ensuring a consistent response format across the
 * entire application.
 * </p>
 *
 * <p><b>How it fits:</b> Used by filters, security handlers, and
 * exception translators — including AI-related exceptions
 * ({@link com.pkmprojects.shoppiq.aiservice.exception.AiAssistantException},
 * {@link com.pkmprojects.shoppiq.aiservice.exception.AiAccessDeniedException}, etc.)
 * to return RFC 9457 problem+json error responses.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Write {@link ProblemDetail} responses.</li>
 *     <li>Set the HTTP status code.</li>
 *     <li>Set the response content type.</li>
 *     <li>Serialize the response using Jackson 3.</li>
 * </ul>
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
