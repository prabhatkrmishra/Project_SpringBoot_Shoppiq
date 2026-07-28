package com.pkmprojects.shoppiq.exception.formatter;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.stream.Collectors;

/**
 * <strong>Spring Boot Concept:</strong> Utility class that formats Jakarta
 * Bean Validation errors into a human-readable message for RFC 9457
 * responses.
 *
 * <p>Exclusively used for {@link MethodArgumentNotValidException} generated
 * by Jakarta Bean Validation ({@code @Valid} / {@code @Validated} on
 * {@code @RequestBody} parameters). Separates the detection of validation
 * errors (handled by Spring's validation infrastructure) from their
 * presentation (this formatter).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class ValidationErrorFormatter {

    /**
     * Prevents instantiation.
     */
    private ValidationErrorFormatter() {
        throw new UnsupportedOperationException("ValidationErrorFormatter is a utility class and cannot be instantiated.");
    }

    /**
     * Formats validation errors into a readable string.
     *
     * @param bindingResult Bean Validation binding Result
     * @return formatted validation message
     */
    public static String format(BindingResult bindingResult) {

        if (!bindingResult.hasErrors()) {
            return ErrorCode.VALIDATION_FAILED.getDefaultMessage();
        }

        var allErrors = new java.util.ArrayList<String>();

        for (var fieldError : bindingResult.getFieldErrors()) {
            allErrors.add(formatFieldError(fieldError));
        }

        for (var globalError : bindingResult.getGlobalErrors()) {
            allErrors.add(globalError.getDefaultMessage());
        }

        return String.join(System.lineSeparator(), allErrors);
    }

    /**
     * Formats a single field validation error.
     *
     * @param fieldError validation field error
     * @return formatted field error
     */
    private static String formatFieldError(FieldError fieldError) {
        return "%s: %s".formatted(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
