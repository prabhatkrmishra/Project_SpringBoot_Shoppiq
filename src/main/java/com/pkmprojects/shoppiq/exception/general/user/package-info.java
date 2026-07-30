/**
 * User-specific exception hierarchy for user management operations.
 *
 * <p>This package contains exceptions for user not-found and duplicate
 * user errors. These exceptions are thrown during user registration,
 * profile updates, and user lookups. The {@link UserNotFoundException}
 * provides factory methods for ID, email, and username lookups, while
 * {@link DuplicateUserException} handles unique constraint violations.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.exception.general.user;
