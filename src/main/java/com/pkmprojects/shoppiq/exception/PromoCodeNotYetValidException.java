package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

import java.time.Instant;

/**
 * Exception thrown when a promo code is not yet valid.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class PromoCodeNotYetValidException extends InvalidOperationException {

    private PromoCodeNotYetValidException(String detail) {
        super(ErrorCode.PROMO_CODE_NOT_YET_VALID, detail);
    }

    public static PromoCodeNotYetValidException forCode(String code, Instant validFrom) {
        return new PromoCodeNotYetValidException(
                "Promo code '%s' is not valid until %s.".formatted(code, validFrom)
        );
    }
}
