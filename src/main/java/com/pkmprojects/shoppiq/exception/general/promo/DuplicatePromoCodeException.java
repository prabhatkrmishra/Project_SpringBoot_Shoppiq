package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a promo code with the same code string already exists.
 *
 * <p>This exception is thrown during promo code creation when the submitted
 * code value conflicts with an existing record. It uses the
 * {@link ErrorCode#PROMO_CODE_ALREADY_EXISTS} code and HTTP 409 Conflict
 * status. Promo codes must be unique across the system.</p>
 *
 * <p>The detail message includes the conflicting code (e.g.,
 * "A promo code with code 'SUMMER2026' already exists.") to help the
 * client understand which code caused the conflict. The client should
 * use a different code value.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#PROMO_CODE_ALREADY_EXISTS
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
