package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a promo code is inactive.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class PromoCodeInactiveException extends InvalidOperationException {

    private PromoCodeInactiveException(String detail) {
        super(ErrorCode.PROMO_CODE_INACTIVE, detail);
    }

    public static PromoCodeInactiveException forCode(String code) {
        return new PromoCodeInactiveException(
                "Promo code '%s' is currently inactive.".formatted(code)
        );
    }
}
