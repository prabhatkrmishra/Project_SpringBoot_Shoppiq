package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a requested user cannot be found.
 *
 * <p>
 * This exception represents lookup failures for {@code User} resources.
 * It is typically thrown by the service layer when a user cannot be
 * resolved using its identifier, email address or username.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Represents missing user resources.</li>
 *     <li>Associates the failure with
 *     {@link ErrorCode#USER_NOT_FOUND}.</li>
 *     <li>Provides expressive factory methods for common lookup failures.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>The constructor is private to enforce factory method usage.</li>
 *     <li>Factory methods centralize message creation.</li>
 *     <li>Additional lookup scenarios can be added without modifying callers.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
    public static UserNotFoundException unknown(String info) {
        return new UserNotFoundException(
                "User was not found -> '%s'".formatted(info)
        );
    }
}