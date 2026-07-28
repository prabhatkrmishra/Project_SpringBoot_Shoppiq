package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

import java.time.Instant;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a promo code
 * has expired.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) for temporal validation (validUntil check).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class PromoCodeExpiredException extends InvalidOperationException {

    private PromoCodeExpiredException(String detail) {
        super(ErrorCode.PROMO_CODE_EXPIRED, detail);
    }

    /**
     * Creates an exception for an expired promo code.
     *
     * @param code       the promo code
     * @param validUntil the expiry timestamp
     * @return a new exception instance
     */
    public static PromoCodeExpiredException forCode(String code, Instant validUntil) {
        return new PromoCodeExpiredException(
                "Promo code '%s' expired on %s.".formatted(code, validUntil)
        );
    }
}
