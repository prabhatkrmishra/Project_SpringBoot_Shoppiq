package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when an attempt is made to create a user that already exists.
 *
 * <p>
 * This exception represents violations of the application's uniqueness
 * constraints for user resources, such as duplicate email addresses or
 * usernames.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Represent duplicate user creation attempts.</li>
 *     <li>Associate the failure with {@link ErrorCode#USER_ALREADY_EXISTS}.</li>
 *     <li>Provide expressive factory methods for common duplicate scenarios.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>The constructor is private to enforce the use of factory methods.</li>
 *     <li>Factory methods improve readability and centralize message creation.</li>
 *     <li>Additional duplicate scenarios can be added without changing callers.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class DuplicateUserException extends DuplicateResourceException {

    /**
     * Creates a duplicate user exception.
     *
     * <p>
     * This constructor is intentionally private to ensure all instances are
     * created through the provided factory methods.
     * </p>
     *
     * @param detail detailed description of the duplicate resource
     */
    private DuplicateUserException(String detail) {
        super(ErrorCode.USER_ALREADY_EXISTS, detail);
    }

    /**
     * Creates an exception indicating that the supplied email address
     * already belongs to another user.
     *
     * @param email duplicate email address
     * @return duplicate user exception
     */
    public static DuplicateUserException email(String email) {
        return new DuplicateUserException("User with email '%s' already exists.".formatted(email));
    }

    /**
     * Creates an exception indicating that the supplied username
     * already belongs to another user.
     *
     * @param username duplicate username
     * @return duplicate user exception
     */
    public static DuplicateUserException username(String username) {
        return new DuplicateUserException("User with username '%s' already exists.".formatted(username));
    }

    /**
     * Creates a generic duplicate user exception when the exact
     * conflicting field cannot be determined.
     *
     * <p>
     * This method is primarily intended for database constraint violations
     * where the underlying persistence provider does not expose the exact
     * violated constraint.
     * </p>
     *
     * @return duplicate user exception
     */
    public static DuplicateUserException unknown() {
        return new DuplicateUserException("A user with the supplied credentials already exists.");
    }
}