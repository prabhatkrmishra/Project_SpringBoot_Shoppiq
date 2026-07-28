package com.pkmprojects.shoppiq.exception.base;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * <strong>Spring Boot Concept:</strong> The root abstract class for all
 * application-specific exceptions in Shoppiq.
 *
 * <p>Root of the layered exception hierarchy. Every application exception
 * extends this class and carries its own {@link ErrorCode}, HTTP status, and
 * detail message, enabling the
 * {@link com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler}
 * to build a consistent RFC 9457 {@code ProblemDetail} response without
 * inspecting exception types.</p>
 *
 * <p>Three layers are used: a shared base layer ({@code ShoppiqException}),
 * a category layer (abstract subclasses grouped by HTTP semantics), and a
 * concrete layer (final subclasses for specific error conditions).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Getter
public abstract class ShoppiqException extends RuntimeException {

    /**
     * Stable machine-readable error code.
     */
    private final ErrorCode errorCode;

    /**
     * HTTP status associated with the exception.
     */
    private final HttpStatus httpStatus;

    /**
     * Detailed description of the error.
     */
    private final String detail;

    /**
     * Creates a new Shoppiq exception.
     *
     * @param errorCode  stable application error code
     * @param httpStatus HTTP status associated with the error
     * @param detail     detailed error description
     */
    protected ShoppiqException(ErrorCode errorCode, HttpStatus httpStatus, String detail) {
        super(detail);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.detail = detail;
    }
}
