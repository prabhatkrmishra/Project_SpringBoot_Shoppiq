package com.pkmprojects.shoppiq.exception.general.address;

import com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a user attempts to access an address they do not own.
 *
 * <p>This exception is thrown when a user attempts to access or modify an
 * address that belongs to another user. Addresses are private and can
 * only be managed by the owner. It uses the
 * {@link ErrorCode#ADDRESS_ACCESS_DENIED} code and HTTP 403 Forbidden
 * status.</p>
 *
 * <p>The detail message includes the address identifier (e.g.,
 * "You are not allowed to access address with id '42'.") to help the
 * client understand which address was restricted. The client should
 * ensure they are operating on their own addresses.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ADDRESS_ACCESS_DENIED
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
