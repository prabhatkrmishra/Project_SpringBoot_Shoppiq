package com.pkmprojects.shoppiq.exception.general.seller;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a seller cannot be found by ID or user ID.
 *
 * <p>This exception is thrown by seller service methods when a database
 * lookup for a seller profile fails. It uses the
 * {@link ErrorCode#SELLER_NOT_FOUND} code and HTTP 404 Not Found status.
 * The seller may not have completed the registration flow, or the
 * identifier may be incorrect.</p>
 *
 * <p>The detail message includes the lookup identifier and type (e.g.,
 * "Seller with id '42' was not found.") to help clients understand which
 * identifier was invalid. The client should verify the identifier and
 * retry the request.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#SELLER_NOT_FOUND
 * @since 1.0.0
 */
public final class SellerNotFoundException extends ResourceNotFoundException {

    private SellerNotFoundException(String detail) {
        super(ErrorCode.SELLER_NOT_FOUND, detail);
    }

    /**
     * Creates an exception for a seller not found by ID.
     *
     * @param id the seller ID
     * @return a new exception instance
     */
    public static SellerNotFoundException id(Long id) {
        return new SellerNotFoundException("Seller with id '%d' was not found.".formatted(id));
    }

    /**
     * Creates an exception for a seller not found by user ID.
     *
     * @param userId the user ID
     * @return a new exception instance
     */
    public static SellerNotFoundException userId(Long userId) {
        return new SellerNotFoundException("Seller for user '%d' was not found.".formatted(userId));
    }
}
