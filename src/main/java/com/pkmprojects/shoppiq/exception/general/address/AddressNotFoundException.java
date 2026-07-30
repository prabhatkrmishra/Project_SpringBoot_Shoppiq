package com.pkmprojects.shoppiq.exception.general.address;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when an address cannot be found by ID.
 *
 * <p>This exception is thrown by address service methods when a database
 * lookup for an address fails. It uses the
 * {@link ErrorCode#ADDRESS_NOT_FOUND} code and HTTP 404 Not Found
 * status. The address may have been deleted by the user.</p>
 *
 * <p>The detail message includes the address identifier (e.g.,
 * "Address with id '42' was not found.") to help the client understand
 * which address was invalid. The client should verify the address ID
 * and retry the request.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ADDRESS_NOT_FOUND
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
