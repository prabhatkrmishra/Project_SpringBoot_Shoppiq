package com.pkmprojects.shoppiq.exception.factory;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProblemDetailFactory}.
 *
 * <p>
 * These tests verify that the factory correctly creates RFC 9457
 * {@link ProblemDetail} instances for both application-specific
 * and framework exceptions.
 * </p>
 *
 * <h2>Test Coverage</h2>
 * <ul>
 *     <li>Create ProblemDetail from {@link ShoppiqException}.</li>
 *     <li>Create ProblemDetail from HTTP status.</li>
 *     <li>Verify standard RFC 9457 fields.</li>
 *     <li>Verify Shoppiq custom properties.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@DisplayName("ProblemDetailFactory Tests")
class ProblemDetailFactoryTest {

    /**
     * Sample URI used during testing.
     */
    private static final URI INSTANCE = URI.create("/api/test");

    /**
     * Verifies that a ProblemDetail is correctly created from a
     * {@link ShoppiqException}.
     */
    @Test
    @DisplayName("Should create ProblemDetail from ShoppiqException")
    void shouldCreateProblemDetailFromShoppiqException() {

        // Arrange
        ShoppiqException exception = new DummyException();

        // Act
        ProblemDetail problemDetail = ProblemDetailFactory.create(exception, INSTANCE);

        // Assert
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getTitle()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
        assertThat(problemDetail.getDetail()).isEqualTo("Dummy resource not found.");
        assertThat(problemDetail.getInstance()).isEqualTo(INSTANCE);
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", ErrorCode.ITEM_NOT_FOUND.getCode());
        assertThat(problemDetail.getProperties()).containsKey("timestamp");
    }

    /**
     * Verifies that a ProblemDetail is correctly created from
     * framework information.
     */
    @Test
    @DisplayName("Should create ProblemDetail from HTTP status")
    void shouldCreateProblemDetailFromStatus() {

        // Arrange

        // Act
        ProblemDetail problemDetail = ProblemDetailFactory.create(HttpStatus.BAD_REQUEST,
                "Validation failed.", ErrorCode.VALIDATION_FAILED, INSTANCE);

        // Assert
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(problemDetail.getDetail()).isEqualTo("Validation failed.");
        assertThat(problemDetail.getInstance()).isEqualTo(INSTANCE);
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", ErrorCode.VALIDATION_FAILED.getCode());
        assertThat(problemDetail.getProperties()).containsKey("timestamp");
    }

    /**
     * Verifies that the timestamp property is populated.
     */
    @Test
    @DisplayName("Should populate timestamp")
    void shouldPopulateTimestamp() {

        // Arrange
        ShoppiqException exception = new DummyException();

        // Act
        ProblemDetail problemDetail = ProblemDetailFactory.create(exception, INSTANCE);

        // Assert
        assertThat(problemDetail.getProperties()).containsKey("timestamp");
        assertThat(problemDetail.getProperties().get("timestamp")).isNotNull();
    }

    /**
     * Verifies that the error code property is populated.
     */
    @Test
    @DisplayName("Should populate error code")
    void shouldPopulateErrorCode() {

        // Arrange
        ShoppiqException exception = new DummyException();

        // Act
        ProblemDetail problemDetail = ProblemDetailFactory.create(exception, INSTANCE);

        // Assert
        assertThat(problemDetail.getProperties()).containsEntry("errorCode", ErrorCode.ITEM_NOT_FOUND.getCode());
    }

    /**
     * Dummy application exception used for testing.
     */
    private static final class DummyException extends ShoppiqException {

        private DummyException() {
            super(ErrorCode.ITEM_NOT_FOUND, HttpStatus.NOT_FOUND, "Dummy resource not found.");
        }
    }
}