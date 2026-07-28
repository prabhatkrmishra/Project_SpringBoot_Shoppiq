package com.pkmprojects.shoppiq.exception.general.address;

import com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a user attempts
 * to access an address they do not own.
 *
 * <p>Leaf exception in the authorization hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException}
 * (HTTP 403) for address resource ownership violations.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class AddressAccessDeniedException extends UnauthorizedOperationException {

    private AddressAccessDeniedException(String detail) {
        super(ErrorCode.ADDRESS_ACCESS_DENIED, detail);
    }

    /**
     * Creates an exception for a user attempting to access an address they do not own.
     *
     * @param addressId the address ID that was accessed without authorization
     * @return a new exception instance
     */
    public static AddressAccessDeniedException forAddress(Long addressId) {
        return new AddressAccessDeniedException(
                "You are not allowed to access address with id '%d'.".formatted(addressId)
        );
    }
}
