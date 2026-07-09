package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a requested promo code could not be found.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class PromoCodeNotFoundException extends ResourceNotFoundException {

    private PromoCodeNotFoundException(String detail) {
        super(ErrorCode.PROMO_CODE_NOT_FOUND, detail);
    }

    public static PromoCodeNotFoundException forCode(String code) {
        return new PromoCodeNotFoundException(
                "Promo code '%s' was not found.".formatted(code)
        );
    }

    public static PromoCodeNotFoundException forId(Long id) {
        return new PromoCodeNotFoundException(
                "Promo code with id '%d' was not found.".formatted(id)
        );
    }
}
