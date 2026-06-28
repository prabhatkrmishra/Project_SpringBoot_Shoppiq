package com.pkmprojects.shoppiq.auth.handler;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.exception.constants.ProblemDetailProperties;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ShoppiqAccessDeniedHandler}.
 *
 * <p>
 * Verifies that authorization failures are converted into RFC 9457
 * {@link ProblemDetail} responses.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShoppiqAccessDeniedHandler Tests")
class ShoppiqAccessDeniedHandlerTest {

    @Mock
    private ProblemDetailResponseWriter responseWriter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private ShoppiqAccessDeniedHandler accessDeniedHandler;

    /**
     * Verifies that authorization failures produce a RFC 9457 response.
     *
     * @throws IOException      if writing the response fails
     * @throws ServletException if servlet processing fails
     */
    @Test
    @DisplayName("Should write forbidden ProblemDetail")
    void shouldWriteForbiddenProblemDetail() throws IOException, ServletException {

        // Arrange
        when(request.getRequestURI()).thenReturn("/api/admin/users");

        AccessDeniedException exception = new AccessDeniedException("Access denied.");
        ArgumentCaptor<ProblemDetail> captor = ArgumentCaptor.forClass(ProblemDetail.class);

        // Act
        accessDeniedHandler.handle(request, response, exception);

        // Assert
        verify(responseWriter).write(eq(response), captor.capture());
        ProblemDetail problemDetail = captor.getValue();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(problemDetail.getTitle()).isEqualTo(HttpStatus.FORBIDDEN.getReasonPhrase());
        assertThat(problemDetail.getProperties())
                .containsEntry(ProblemDetailProperties.ERROR_CODE, ErrorCode.ACCESS_DENIED.getCode());
    }

}