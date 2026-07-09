package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

import java.time.Instant;

/**
 * Exception thrown when a promo code has expired.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class PromoCodeExpiredException extends InvalidOperationException {

    private PromoCodeExpiredException(String detail) {
        super(ErrorCode.PROMO_CODE_EXPIRED, detail);
    }

    public static PromoCodeExpiredException forCode(String code, Instant validUntil) {
        return new PromoCodeExpiredException(
                "Promo code '%s' expired on %s.".formatted(code, validUntil)
        );
    }
}
