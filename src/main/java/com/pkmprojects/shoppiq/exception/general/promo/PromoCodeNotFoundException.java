package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a requested
 * promo code could not be found.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) with factory methods for lookup by code string and by ID.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class PromoCodeNotFoundException extends ResourceNotFoundException {

    private PromoCodeNotFoundException(String detail) {
        super(ErrorCode.PROMO_CODE_NOT_FOUND, detail);
    }

    /**
     * Creates an exception for a promo code not found by its code string.
     *
     * @param code the promo code string
     * @return a new exception instance
     */
    public static PromoCodeNotFoundException forCode(String code) {
        return new PromoCodeNotFoundException(
                "Promo code '%s' was not found.".formatted(code)
        );
    }

    /**
     * Creates an exception for a promo code not found by its ID.
     *
     * @param id the promo code ID
     * @return a new exception instance
     */
    public static PromoCodeNotFoundException forId(Long id) {
        return new PromoCodeNotFoundException(
                "Promo code with id '%d' was not found.".formatted(id)
        );
    }
}
