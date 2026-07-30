package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown for generic request validation failures not mapped to a specific domain rule.
 *
 * <p>This exception is used when a request fails validation but does not
 * fit into a more specific exception category. It uses the
 * {@link ErrorCode#INVALID_OPERATION} code and HTTP 400 Bad Request
 * status. For domain-specific validation failures, prefer using the
 * dedicated exception classes (e.g., {@link CurrentPasswordIncorrectException},
 * {@link PasswordChangeException}).</p>
 *
 * <p>This exception follows the static factory method pattern. Use
 * {@link #detail(String)} to create an instance with a descriptive
 * message explaining what validation failed and how to fix it.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#INVALID_OPERATION
 * @since 1.0.0
 */
public final class InvalidRequestException extends InvalidOperationException {

    private InvalidRequestException(String detail) {
        super(ErrorCode.INVALID_OPERATION, detail);
    }

    /**
     * Creates an exception with the given detail message.
     *
     * @param detail the validation failure description
     * @return invalid request exception
     */
    public static InvalidRequestException detail(String detail) {
        return new InvalidRequestException(detail);
    }
}
