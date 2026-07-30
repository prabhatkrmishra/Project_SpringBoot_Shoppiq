package com.pkmprojects.shoppiq.exception.general.seller;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a seller profile already exists for a user.
 *
 * <p>This exception is thrown during seller registration when the
 * authenticated user already has an associated seller profile. Each user
 * can have at most one seller profile. It uses the
 * {@link ErrorCode#SELLER_ALREADY_EXISTS} code and HTTP 409 Conflict
 * status.</p>
 *
 * <p>The detail message includes the conflicting identifier (e.g.,
 * "A seller profile for user '42' already exists.") to help the client
 * understand which field caused the conflict. The client should use
 * their existing seller profile or contact support.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#SELLER_ALREADY_EXISTS
 * @since 1.0.0
 */
public final class SellerAlreadyExistsException extends DuplicateResourceException {

    private SellerAlreadyExistsException(String detail) {
        super(ErrorCode.SELLER_ALREADY_EXISTS, detail);
    }

    /**
     * Creates an exception for a user who already has a seller profile.
     *
     * @param userId the user ID
     * @return a new exception instance
     */
    public static SellerAlreadyExistsException forUser(Long userId) {
        return new SellerAlreadyExistsException("A seller profile for user '%d' already exists.".formatted(userId));
    }

    /**
     * Creates an exception for a business email already in use by another seller.
     *
     * @param email the business email
     * @return a new exception instance
     */
    public static SellerAlreadyExistsException forEmail(String email) {
        return new SellerAlreadyExistsException("A seller with business email '%s' already exists.".formatted(email));
    }
}
