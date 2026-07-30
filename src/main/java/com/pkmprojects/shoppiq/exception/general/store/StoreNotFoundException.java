package com.pkmprojects.shoppiq.exception.general.store;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a store cannot be found for a given seller.
 *
 * <p>This exception is thrown by store service methods when a database
 * lookup for a store fails. Each seller has one associated store that is
 * created during the seller registration flow. It uses the
 * {@link ErrorCode#STORE_NOT_FOUND} code and HTTP 404 Not Found status.</p>
 *
 * <p>The detail message includes the seller identifier (e.g.,
 * "No store found for seller '42'.") to help the client understand which
 * seller's store was missing. The seller should complete the registration
 * flow to create a store.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#STORE_NOT_FOUND
 * @since 1.0.0
 */
public final class StoreNotFoundException extends ResourceNotFoundException {

    private StoreNotFoundException(String detail) {
        super(ErrorCode.STORE_NOT_FOUND, detail);
    }

    /**
     * Creates an exception indicating that no store exists for the
     * given seller.
     *
     * @param sellerId the seller identifier
     * @return store not found exception
     */
    public static StoreNotFoundException forSeller(Long sellerId) {
        return new StoreNotFoundException(
                "No store found for seller '%d'.".formatted(sellerId)
        );
    }
}
