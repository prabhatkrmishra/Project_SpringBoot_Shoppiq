package com.pkmprojects.shoppiq.exception.codes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ErrorCode}.
 *
 * <p>
 * These tests verify the integrity of the application's machine-readable
 * error code registry. Since {@link ErrorCode} forms part of the public API
 * contract, the values defined here must remain stable across releases.
 * </p>
 *
 * <h2>Test Coverage</h2>
 * <ul>
 *     <li>Every error code exposes a non-null code.</li>
 *     <li>Every error code exposes a non-blank default message.</li>
 *     <li>All error codes are unique.</li>
 *     <li>Enum lookup behaves correctly.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@DisplayName("ErrorCode Tests")
class ErrorCodeTest {

    /**
     * Verifies that every {@link ErrorCode} exposes a machine-readable code.
     */
    @Test
    @DisplayName("Every error code should expose a non-null code")
    void shouldExposeNonNullCode() {

        // Arrange & Act
        ErrorCode[] errorCodes = ErrorCode.values();

        // Assert
        for (ErrorCode errorCode : errorCodes) {
            assertThat(errorCode.getCode()).isNotNull().isNotBlank();
        }
    }

    /**
     * Verifies that every {@link ErrorCode} exposes a default message.
     */
    @Test
    @DisplayName("Every error code should expose a default message")
    void shouldExposeDefaultMessage() {

        // Arrange & Act
        ErrorCode[] errorCodes = ErrorCode.values();

        // Assert
        for (ErrorCode errorCode : errorCodes) {
            assertThat(errorCode.getDefaultMessage()).isNotNull().isNotBlank();
        }
    }

    /**
     * Verifies that every machine-readable error code is unique.
     */
    @Test
    @DisplayName("Error codes should be unique")
    void shouldContainUniqueErrorCodes() {

        // Arrange
        Set<String> uniqueCodes = Arrays.stream(ErrorCode.values()).map(ErrorCode::getCode).collect(Collectors.toSet());

        // Act & Assert
        assertThat(uniqueCodes).hasSameSizeAs(ErrorCode.values());
    }

    /**
     * Verifies that enum lookup by name works as expected.
     */
    @Test
    @DisplayName("Should resolve enum by name")
    void shouldResolveEnumByName() {

        // Arrange & Act
        ErrorCode errorCode = ErrorCode.valueOf("ITEM_NOT_FOUND");

        // Assert
        assertThat(errorCode).isEqualTo(ErrorCode.ITEM_NOT_FOUND);
        assertThat(errorCode.getCode()).isEqualTo("ITEM-404-001");
    }

    /**
     * Verifies that no duplicate enum constants exist.
     */
    @Test
    @DisplayName("Enum should not contain duplicate constants")
    void shouldNotContainDuplicateEnumConstants() {

        // Arrange
        Set<ErrorCode> uniqueConstants = Arrays.stream(ErrorCode.values()).collect(Collectors.toSet());

        // Act & Assert
        assertThat(uniqueConstants).hasSameSizeAs(ErrorCode.values());
    }

}