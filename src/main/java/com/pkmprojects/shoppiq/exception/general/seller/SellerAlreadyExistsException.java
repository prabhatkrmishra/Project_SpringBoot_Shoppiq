package com.pkmprojects.shoppiq.exception.general.seller;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a seller
 * profile already exists for a user.
 *
 * <p>Leaf exception in the duplicate-resource hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.DuplicateResourceException}
 * (HTTP 409) with factory methods for duplicate user and duplicate
 * business email scenarios.</p>
 *
 * @author prabhatkrmishra
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
