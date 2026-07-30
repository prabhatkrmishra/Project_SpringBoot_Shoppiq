package com.pkmprojects.shoppiq.exception.base;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Root abstract class for all application-specific exceptions in Shoppiq.
 *
 * <p>This class serves as the base of the exception hierarchy and carries
 * three critical pieces of information: a stable {@link ErrorCode} for
 * machine-readable identification, an {@link HttpStatus} for HTTP response
 * mapping, and a human-readable detail message for client consumption.
 * Every exception in the application extends this class, ensuring that
 * the global exception handler can uniformly convert any application error
 * into an RFC 9457 Problem Detail response.</p>
 *
 * <p>Architecturally, this class decouples the error detection site from
 * the error presentation layer. Service methods throw subclasses of this
 * exception without worrying about HTTP status codes or response formats.
 * The {@link com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler}
 * reads the exception properties and constructs the appropriate response.
 * This separation of concerns makes it easy to change error presentation
 * without modifying business logic.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode
 * @see com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler
 * @since 1.0.0
 */
@Getter
public abstract class ShoppiqException extends RuntimeException {

    /**
     * Stable machine-readable error code that uniquely identifies this error type.
     *
     * <p>Error codes follow the pattern {@code MODULE-HTTP_STATUS-SEQUENCE}
     * and are defined in the {@link ErrorCode} enum. They form part of the
     * public API contract and must never change once released, as clients
     * may rely on them for automated error handling.</p>
     */
    private final ErrorCode errorCode;

    /**
     * HTTP status associated with this exception.
     *
     * <p>This status is used by the global exception handler to set the
     * HTTP response status code. It must match the semantics of the
     * {@link ErrorCode} to ensure consistent behavior across the API.</p>
     */
    private final HttpStatus httpStatus;

    /**
     * Detailed human-readable description of the error.
     *
     * <p>This message is included in the Problem Detail response's
     * {@code detail} field and should provide enough information for the
     * client to understand what went wrong and how to fix it. Unlike the
     * error code, this message may change between versions.</p>
     */
    private final String detail;

    /**
     * Creates a new Shoppiq exception with the specified error code, HTTP
     * status, and detail message.
     *
     * <p>The detail message is also passed to the {@link RuntimeException}
     * superclass constructor, making it available through the standard
     * {@link #getMessage()} method for logging and debugging purposes.</p>
     *
     * @param errorCode  the stable application error code
     * @param httpStatus the HTTP status to return in the response
     * @param detail     the human-readable error description
     */
    protected ShoppiqException(ErrorCode errorCode, HttpStatus httpStatus, String detail) {
        super(detail);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.detail = detail;
    }
}
