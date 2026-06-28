package com.pkmprojects.shoppiq.exception.base;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all application-specific exceptions in Shoppiq.
 *
 * <p>
 * This class provides a common structure for every business exception by
 * encapsulating the information required to generate a standardized RFC 9457
 * {@code ProblemDetail} response.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Stores the machine-readable {@link ErrorCode}.</li>
 *     <li>Stores the associated HTTP status.</li>
 *     <li>Stores the response detail message.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>All custom exceptions should extend this class.</li>
 *     <li>Exception handlers should never contain business logic.</li>
 *     <li>This class represents business failure information only.</li>
 * </ul>
 *
 * <h2>Future Scope</h2>
 * <ul>
 *     <li>Support documentation URLs.</li>
 *     <li>Support localization.</li>
 *     <li>Support correlation identifiers.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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