package com.pkmprojects.shoppiq.util.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProblemDetailResponseWriter}.
 *
 * <p>
 * Verifies that RFC 9457 {@link ProblemDetail} responses are correctly
 * written to the HTTP response.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProblemDetailResponseWriter Tests")
class ProblemDetailResponseWriterTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletOutputStream outputStream;

    @InjectMocks
    private ProblemDetailResponseWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        when(response.getOutputStream()).thenReturn(outputStream);
    }

    /**
     * Verifies that a ProblemDetail is written correctly.
     *
     * @throws IOException if response writing fails
     */
    @Test
    @DisplayName("Should write RFC9457 ProblemDetail response")
    void shouldWriteProblemDetailResponse() throws IOException {

        // Arrange
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Item not found.");

        // Act
        responseWriter.write(response, problemDetail);

        // Assert
        verify(response).setStatus(HttpStatus.NOT_FOUND.value());
        verify(response).setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        verify(objectMapper).writeValue(eq(outputStream), eq(problemDetail));
    }

}