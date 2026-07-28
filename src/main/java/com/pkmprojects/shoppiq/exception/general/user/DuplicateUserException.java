package com.pkmprojects.shoppiq.exception.general.user;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when an attempt
 * is made to create a user that already exists.
 *
 * <p>Leaf exception in the duplicate-resource hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.DuplicateResourceException}
 * (HTTP 409) with factory methods for duplicate email, duplicate username,
 * and a generic fallback for constraint violations where the exact
 * conflicting field cannot be determined.</p>
 *
 * @author prabhatkrmishra
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
