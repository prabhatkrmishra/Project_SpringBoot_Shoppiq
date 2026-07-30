package com.pkmprojects.shoppiq.exception.handler;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.http.client.FilteredHostException;
import org.springframework.boot.http.client.InetAddressFilter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.lang.reflect.Constructor;

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

    @Test
    @DisplayName("handleFilteredHostException should return 403 ProblemDetail")
    void handleFilteredHostExceptionShouldReturnForbidden() throws Exception {

        // Arrange - use reflection to create FilteredHostException (constructor is package-private)
        InetAddressFilter filter = InetAddressFilter.externalAddresses();
        Constructor<FilteredHostException> ctor = FilteredHostException.class.getDeclaredConstructor(String.class, InetAddressFilter.class);
        ctor.setAccessible(true);
        FilteredHostException exception = ctor.newInstance("localhost", filter);
        request.setRequestURI("/api/test");

        // Act
        ProblemDetail result = handler.handleFilteredHostException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(result.getDetail()).contains("blocked by security policy");
        assertThat(result.getProperties()).containsEntry("errorCode", ErrorCode.ACCESS_DENIED.getCode());
    }

    @Test
    @DisplayName("handleDataIntegrityViolationException should return 409 ProblemDetail")
    void handleDataIntegrityViolationShouldReturnConflict() {

        // Arrange
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("Column 'created_at' cannot be null");
        request.setRequestURI("/api/ai/chat");

        // Act
        ProblemDetail result = handler.handleDataIntegrityViolationException(exception, request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getDetail()).contains("conflicts with existing data");
        assertThat(result.getProperties()).containsEntry("errorCode", ErrorCode.DATA_INTEGRITY_VIOLATION.getCode());
    }
}
