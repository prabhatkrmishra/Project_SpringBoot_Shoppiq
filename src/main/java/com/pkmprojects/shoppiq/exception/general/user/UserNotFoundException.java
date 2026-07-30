package com.pkmprojects.shoppiq.exception.general.user;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a requested user cannot be found by ID, email, or username.
 *
 * <p>This exception is thrown by user service methods when a database
 * lookup for a user fails. It uses the {@link ErrorCode#USER_NOT_FOUND}
 * code and HTTP 404 Not Found status. The exception provides multiple
 * static factory methods to create instances for different lookup
 * scenarios, each with a descriptive detail message.</p>
 *
 * <p>The detail message includes the lookup identifier and type (e.g.,
 * "User with id '42' was not found.") to help clients understand which
 * identifier was invalid. The client should verify the identifier and
 * retry the request.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#USER_NOT_FOUND
 * @since 1.0.0
 */
public final class UserNotFoundException extends ResourceNotFoundException {

    /**
     * Creates a user not found exception.
     *
     * @param detail detailed error description
     */
    private UserNotFoundException(String detail) {
        super(ErrorCode.USER_NOT_FOUND, detail);
    }

    /**
     * Creates an exception indicating that no user exists with the
     * supplied identifier.
     *
     * @param id user identifier
     * @return user not found exception
     */
    public static UserNotFoundException id(Long id) {
        return new UserNotFoundException(
                "User with id '%d' was not found.".formatted(id)
        );
    }

    /**
     * Creates an exception indicating that no user exists with the
     * supplied email address.
     *
     * @param email user email
     * @return user not found exception
     */
    public static UserNotFoundException email(String email) {
        return new UserNotFoundException(
                "User with email '%s' was not found.".formatted(email)
        );
    }

    /**
     * Creates an exception indicating that no user exists with the
     * supplied username.
     *
     * @param username username
     * @return user not found exception
     */
    public static UserNotFoundException username(String username) {
        return new UserNotFoundException(
                "User with username '%s' was not found.".formatted(username)
        );
    }

    /**
     * Creates a generic user not found exception with additional context.
     *
     * <p>This factory method is used when the specific lookup type
     * (ID, email, or username) is not known or when additional context
     * needs to be included in the error message. The detail message
     * includes the provided information for diagnostics.</p>
     *
     * @param info additional context about the lookup
     * @return a new exception instance
     */
    public static UserNotFoundException unknown(String info) {
        return new UserNotFoundException(
                "User was not found -> '%s'".formatted(info)
        );
    }
}
