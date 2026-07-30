package com.pkmprojects.shoppiq.exception.general.banner;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a requested homepage banner cannot be found.
 *
 * <p>This exception is thrown by banner service methods when a database
 * lookup for a banner fails. Banners are used on the homepage to promote
 * sales and featured products. It uses the
 * {@link ErrorCode#BANNER_NOT_FOUND} code and HTTP 404 Not Found status.</p>
 *
 * <p>The detail message includes the banner identifier (e.g.,
 * "Banner with id '42' was not found.") to help the client understand
 * which banner was invalid. The administrator should verify the banner
 * ID and retry the operation.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#BANNER_NOT_FOUND
 * @since 1.0.0
 */
public final class BannerNotFoundException extends ResourceNotFoundException {

    private BannerNotFoundException(String detail) {
        super(ErrorCode.BANNER_NOT_FOUND, detail);
    }

    /**
     * Creates an exception for a banner not found by its identifier.
     *
     * @param id the banner ID that was not found
     * @return a new exception instance
     */
    public static BannerNotFoundException forId(Long id) {
        return new BannerNotFoundException(
                "Banner with id '%d' was not found.".formatted(id)
        );
    }
}
