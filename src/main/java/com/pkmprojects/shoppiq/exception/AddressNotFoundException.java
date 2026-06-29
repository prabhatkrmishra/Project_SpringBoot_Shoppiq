package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when an address cannot be found.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class AddressNotFoundException extends ResourceNotFoundException {

    private AddressNotFoundException(String detail) {
        super(ErrorCode.ADDRESS_NOT_FOUND, detail);
    }

    public static AddressNotFoundException id(Long id) {
        return new AddressNotFoundException(
                "Address with id '%d' was not found.".formatted(id)
        );
    }
}
