package com.pkmprojects.shoppiq.exception.general.seller;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a seller
 * cannot be found.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) with factory methods for lookup by seller ID and by user ID.</p>
 *
 * @author prabhatkrmishra
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
