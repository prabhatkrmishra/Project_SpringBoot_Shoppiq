package com.pkmprojects.shoppiq.exception.general.address;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when an address
 * cannot be found.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) and targets missing
 * {@link com.pkmprojects.shoppiq.entity.address.Address} entities.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class AddressNotFoundException extends ResourceNotFoundException {

    private AddressNotFoundException(String detail) {
        super(ErrorCode.ADDRESS_NOT_FOUND, detail);
    }

    /**
     * Creates an exception for an address not found by its identifier.
     *
     * @param id the address ID that was not found
     * @return a new exception instance
     */
    public static AddressNotFoundException id(Long id) {
        return new AddressNotFoundException(
                "Address with id '%d' was not found.".formatted(id)
        );
    }
}
