package com.pkmprojects.shoppiq.exception.formatter;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Utility class that formats Jakarta Bean Validation errors into human-readable
 * messages for inclusion in Problem Detail responses.
 *
 * <p>This formatter processes the {@link BindingResult} from a
 * {@link MethodArgumentNotValidException} and produces a single string
 * containing all field-level and global-level validation errors. Each
 * field error is formatted as {@code "fieldName: errorMessage"} and global
 * errors are included with their default messages. Errors are separated
 * by line breaks for readability.</p>
 *
 * <p>The formatted output is used by the
 * {@link com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler}
 * as the {@code detail} field in RFC 9457 Problem Detail responses. This
 * provides clients with specific, actionable information about which fields
 * failed validation and why.</p>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler
 * @since 1.0.0
 */
public final class ValidationErrorFormatter {

    /**
     * Prevents instantiation of this utility class.
     *
     * <p>The constructor throws {@link UnsupportedOperationException} to
     * enforce the static-only usage pattern. All methods in this class are
     * static and should be called directly on the class.</p>
     */
    private ValidationErrorFormatter() {
        throw new UnsupportedOperationException("ValidationErrorFormatter is a utility class and cannot be instantiated.");
    }

    /**
     * Formats all validation errors from the binding result into a single
     * human-readable string.
     *
     * <p>If no errors are present, returns the default validation failed
     * message. Otherwise, iterates over all field errors (formatting each
     * as {@code "field: message"}) and global errors (using their default
     * messages), joining them with line breaks for readability.</p>
     *
     * @param bindingResult the Bean Validation binding result containing all errors
     * @return a formatted string of all validation errors
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
     * Formats a single field validation error into a readable string.
     *
     * <p>The format is {@code "fieldName: defaultMessage"}, where fieldName
     * is the path to the invalid field and defaultMessage is the violation
     * message from the validation annotation.</p>
     *
     * @param fieldError the validation field error to format
     * @return the formatted field error string
     */
    private static String formatFieldError(FieldError fieldError) {
        return "%s: %s".formatted(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
