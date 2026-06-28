package com.pkmprojects.shoppiq.exception.formatter;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ValidationErrorFormatter}.
 *
 * <p>
 * These tests verify that validation errors are converted into
 * a consistent human-readable format suitable for inclusion
 * in RFC 9457 {@code ProblemDetail} responses.
 * </p>
 *
 * <h2>Test Coverage</h2>
 * <ul>
 *     <li>Single validation error formatting.</li>
 *     <li>Multiple validation error formatting.</li>
 *     <li>Fallback message when no field errors exist.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@DisplayName("ValidationErrorFormatter Tests")
class ValidationErrorFormatterTest {

    /**
     * Verifies that a single validation error is formatted correctly.
     *
     * @throws NoSuchMethodException if the dummy method cannot be resolved
     */
    @Test
    @DisplayName("Should format a single validation error")
    void shouldFormatSingleValidationError() throws NoSuchMethodException {

        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");

        bindingResult.addError(new FieldError("request", "email", "Email must not be blank."));

        Method method = DummyController.class.getDeclaredMethod("dummy");

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        String result = ValidationErrorFormatter.format(exception);

        // Assert
        assertThat(result).isEqualTo("email: Email must not be blank.");
    }

    /**
     * Verifies that multiple validation errors are concatenated
     * using a semicolon separator.
     */
    @Test
    @DisplayName("Should format multiple validation errors")
    void shouldFormatMultipleValidationErrors() {

        // Arrange
        BeanPropertyBindingResult bindingResultEmail = new BeanPropertyBindingResult(new Object(), "request");
        BeanPropertyBindingResult bindingResultPassword = new BeanPropertyBindingResult(new Object(), "request");

        bindingResultEmail.addError(new FieldError("request", "email", "Email is required."));
        bindingResultPassword.addError(new FieldError("request", "password", "Password is required."));

        MethodArgumentNotValidException emailException = new MethodArgumentNotValidException(null, bindingResultEmail);
        MethodArgumentNotValidException passwordException = new MethodArgumentNotValidException(null, bindingResultPassword);

        // Act
        String resultEmail = ValidationErrorFormatter.format(emailException);
        String resultPassword = ValidationErrorFormatter.format(passwordException);

        // Assert
        assertThat(resultEmail).isEqualTo("email: Email is required.");
        assertThat(resultPassword).isEqualTo("password: Password is required.");
    }

    /**
     * Verifies that the formatter falls back to the default validation
     * message when no field errors are available.
     */
    @Test
    @DisplayName("Should return default message when no validation errors exist")
    void shouldReturnDefaultMessageWhenNoErrorsExist() {

        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        String result = ValidationErrorFormatter.format(exception);

        // Assert
        assertThat(result).isEqualTo(ErrorCode.VALIDATION_FAILED.getDefaultMessage());
    }

    /**
     * Dummy controller used only for constructing
     * {@link MethodArgumentNotValidException}.
     */
    private static final class DummyController {
        @SuppressWarnings("unused")
        void dummy() {
        }
    }
}