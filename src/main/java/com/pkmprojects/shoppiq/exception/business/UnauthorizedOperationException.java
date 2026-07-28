package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * <strong>Spring Boot Concept:</strong> Mid-level category exception that
 * groups all "forbidden" cases under HTTP {@code 403 Forbidden}.
 *
 * <p>Base exception indicating that the current user is authenticated but
 * lacks the required permissions — distinct from
 * {@link com.pkmprojects.shoppiq.exception.auth.AuthenticationException}
 * (401, not authenticated). These two statuses live in separate branches
 * of the exception hierarchy, reflecting their different HTTP semantics
 * and ensuring exception handlers map them to the correct HTTP response.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public abstract class UnauthorizedOperationException extends ShoppiqException {

    /**
     * Creates an unauthorized operation exception.
     *
     * @param errorCode machine-readable error code
     * @param detail    detailed error message
     */
    protected UnauthorizedOperationException(ErrorCode errorCode, String detail) {
        super(errorCode, HttpStatus.FORBIDDEN, detail);
    }

}
