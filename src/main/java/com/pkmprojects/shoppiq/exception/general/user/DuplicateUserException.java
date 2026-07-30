package com.pkmprojects.shoppiq.exception.general.user;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when an attempt is made to create a user that already exists.
 *
 * <p>This exception is thrown during user registration when the submitted
 * email or username conflicts with an existing record. It uses the
 * {@link ErrorCode#USER_ALREADY_EXISTS} code and HTTP 409 Conflict
 * status. The exception provides static factory methods for specific
 * conflict types (email, username) and a generic method for database
 * constraint violations where the exact field is unknown.</p>
 *
 * <p>The detail message includes the conflicting identifier (e.g.,
 * "User with email 'john@example.com' already exists.") to help the
 * client understand which field caused the conflict. The client should
 * use a different value or proceed to the login flow.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#USER_ALREADY_EXISTS
 * @since 1.0.0
 */
public final class DuplicateUserException extends DuplicateResourceException {

    /**
     * Creates a duplicate user exception.
     *
     * <p>This constructor is intentionally private to ensure all instances
     * are created through the provided factory methods. This enforces the
     * use of descriptive static factory methods that clearly indicate
     * which field caused the duplication.</p>
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
     * <p>This method is primarily intended for database constraint
     * violations where the underlying persistence provider does not
     * expose the exact violated constraint. The detail message is
     * generic to avoid information leakage.</p>
     *
     * @return duplicate user exception
     */
    public static DuplicateUserException unknown() {
        return new DuplicateUserException("A user with the supplied credentials already exists.");
    }
}
