package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a promo code
 * with the same code string already exists.
 *
 * <p>Leaf exception in the duplicate-resource hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.DuplicateResourceException}
 * (HTTP 409) for promo code code-string uniqueness enforcement.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class DuplicatePromoCodeException extends DuplicateResourceException {

    private DuplicatePromoCodeException(String detail) {
        super(ErrorCode.PROMO_CODE_ALREADY_EXISTS, detail);
    }

    /**
     * Creates an exception for a duplicate promo code string.
     *
     * @param code the duplicate promo code
     * @return a new exception instance
     */
    public static DuplicatePromoCodeException forCode(String code) {
        return new DuplicatePromoCodeException(
                "A promo code with code '%s' already exists.".formatted(code)
        );
    }
}
