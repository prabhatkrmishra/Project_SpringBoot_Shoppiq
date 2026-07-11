package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a requested homepage banner could not be found.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class BannerNotFoundException extends ResourceNotFoundException {

    private BannerNotFoundException(String detail) {
        super(ErrorCode.BANNER_NOT_FOUND, detail);
    }

    /**
     * Creates an exception for a banner not found by its ID.
     *
     * @param id the banner ID
     * @return the exception
     */
    public static BannerNotFoundException forId(Long id) {
        return new BannerNotFoundException(
                "Banner with id '%d' was not found.".formatted(id)
        );
    }
}
