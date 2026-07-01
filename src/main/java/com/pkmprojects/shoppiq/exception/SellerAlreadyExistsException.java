package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a seller profile already exists for a user.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class SellerAlreadyExistsException extends DuplicateResourceException {

    private SellerAlreadyExistsException(String detail) {
        super(ErrorCode.SELLER_ALREADY_EXISTS, detail);
    }

    public static SellerAlreadyExistsException forUser(Long userId) {
        return new SellerAlreadyExistsException("A seller profile for user '%d' already exists.".formatted(userId));
    }

    public static SellerAlreadyExistsException forEmail(String email) {
        return new SellerAlreadyExistsException("A seller with business email '%s' already exists.".formatted(email));
    }
}
