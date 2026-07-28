package com.pkmprojects.shoppiq.exception.general.user;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a requested
 * user cannot be found.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) with factory methods for lookup by ID, email, username, and
 * a generic fallback for partial information lookups.</p>
 *
 * @author prabhatkrmishra
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
     * Creates an exception indicating that user does not exist
     * with additional info.
     *
     * @param info additional info
     * @return user not found exception
     */
    /**
     * Creates a generic user not found exception with additional context.
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
