package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a user attempts to access an address they do not own.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class AddressAccessDeniedException extends UnauthorizedOperationException {

    private AddressAccessDeniedException(String detail) {
        super(ErrorCode.ADDRESS_ACCESS_DENIED, detail);
    }

    public static AddressAccessDeniedException forAddress(Long addressId) {
        return new AddressAccessDeniedException(
                "You are not allowed to access address with id '%d'.".formatted(addressId)
        );
    }
}
