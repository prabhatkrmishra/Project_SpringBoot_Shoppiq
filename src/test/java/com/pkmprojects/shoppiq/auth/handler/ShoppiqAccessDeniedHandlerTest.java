package com.pkmprojects.shoppiq.auth.handler;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import com.pkmprojects.shoppiq.exception.constants.ProblemDetailProperties;
import jakarta.servlet.RequestDispatcher;
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
 * Verifies that authorization failures are forwarded to the /error page
 * with the correct ProblemDetail attributes set.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShoppiqAccessDeniedHandler Tests")
class ShoppiqAccessDeniedHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @InjectMocks
    private ShoppiqAccessDeniedHandler accessDeniedHandler;

    /**
     * Verifies that authorization failures forward to /error with a 403 ProblemDetail.
     *
     * @throws IOException      if writing the response fails
     * @throws ServletException if servlet processing fails
     */
    @Test
    @DisplayName("Should forward to error page with forbidden ProblemDetail")
    void shouldForwardToErrorPageWithForbiddenProblemDetail() throws IOException, ServletException {

        // Arrange
        when(request.getRequestURI()).thenReturn("/api/admin/users");
        when(request.getRequestDispatcher("/error")).thenReturn(requestDispatcher);

        AccessDeniedException exception = new AccessDeniedException("Access denied.");

        // Act
        accessDeniedHandler.handle(request, response, exception);

        // Assert
        verify(request).setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.FORBIDDEN.value());
        verify(request).setAttribute(RequestDispatcher.ERROR_MESSAGE, ErrorCode.ACCESS_DENIED.getDefaultMessage());
        verify(request).setAttribute("errorCode", ErrorCode.ACCESS_DENIED.getCode());
        verify(requestDispatcher).forward(request, response);
    }

}