package com.pkmprojects.shoppiq.exception.general.banner;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a requested
 * homepage banner could not be found.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) for missing {@link com.pkmprojects.shoppiq.entity.banner.Banner}
 * entities.</p>
 *
 * @author prabhatkrmishra
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
