package com.pkmprojects.shoppiq.auth.entrypoint;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
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
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ShoppiqAuthenticationEntryPoint}.
 *
 * <p>
 * Verifies that authentication failures are converted into RFC 9457
 * ProblemDetail responses.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShoppiqAuthenticationEntryPoint Tests")
class ShoppiqAuthenticationEntryPointTest {

    @Mock
    private ProblemDetailResponseWriter responseWriter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private ShoppiqAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * Verifies that authentication failures are translated into a
     * RFC 9457 ProblemDetail response.
     *
     * @throws IOException      if writing the response fails
     * @throws ServletException if servlet processing fails
     */
    @Test
    @DisplayName("Should write unauthorized ProblemDetail")
    void shouldWriteUnauthorizedProblemDetail() throws IOException, ServletException {

        // Arrange
        when(request.getRequestURI()).thenReturn("/api/items");

        BadCredentialsException exception = new BadCredentialsException("JWT token is invalid.");
        ArgumentCaptor<ProblemDetail> captor = ArgumentCaptor.forClass(ProblemDetail.class);

        // Act
        authenticationEntryPoint.commence(request, response, exception);

        // Assert
        verify(responseWriter).write(eq(response), captor.capture());
        ProblemDetail problemDetail = captor.getValue();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(problemDetail.getDetail()).isEqualTo("JWT token is invalid.");
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", ErrorCode.UNAUTHORIZED.getCode());
    }
}