package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a promo code with the same code string already exists.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class DuplicatePromoCodeException extends DuplicateResourceException {

    private DuplicatePromoCodeException(String detail) {
        super(ErrorCode.PROMO_CODE_NOT_FOUND, detail);
    }

    public static DuplicatePromoCodeException forCode(String code) {
        return new DuplicatePromoCodeException(
                "A promo code with code '%s' already exists.".formatted(code)
        );
    }
}
