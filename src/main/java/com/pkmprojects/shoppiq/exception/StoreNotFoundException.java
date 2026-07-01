package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a store cannot be found.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class StoreNotFoundException extends ResourceNotFoundException {

    private StoreNotFoundException(String detail) {
        super(ErrorCode.STORE_NOT_FOUND, detail);
    }

    public static StoreNotFoundException id(Long id) {
        return new StoreNotFoundException("Store with id '%d' was not found.".formatted(id));
    }

    public static StoreNotFoundException slug(String slug) {
        return new StoreNotFoundException("Store with slug '%s' was not found.".formatted(slug));
    }
}
