package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a seller cannot be found.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class SellerNotFoundException extends ResourceNotFoundException {

    private SellerNotFoundException(String detail) {
        super(ErrorCode.SELLER_NOT_FOUND, detail);
    }

    public static SellerNotFoundException id(Long id) {
        return new SellerNotFoundException("Seller with id '%d' was not found.".formatted(id));
    }

    public static SellerNotFoundException userId(Long userId) {
        return new SellerNotFoundException("Seller for user '%d' was not found.".formatted(userId));
    }
}
