package com.pkmprojects.shoppiq.exception.handler;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    @DisplayName("handleClientAbortException should return null")
    void handleClientAbortExceptionShouldReturnNull() {

        // Arrange
        ClientAbortException exception = new ClientAbortException(new IOException("Connection reset"));
        request.setRequestURI("/css/main.css");

        // Act
        Object result = handler.handleClientAbortException(exception, request);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("handleUnexpectedException should return ProblemDetail with 500 status")
    void handleUnexpectedExceptionShouldReturnProblemDetail() {

        // Arrange
        Exception exception = new RuntimeException("Something went wrong");
        request.setRequestURI("/api/test");

        // Act
        ProblemDetail result = handler.handleUnexpectedException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getProperties()).containsEntry("errorCode", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
    }
}
