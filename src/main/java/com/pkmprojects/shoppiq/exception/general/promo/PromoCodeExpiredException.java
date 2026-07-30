package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

import java.time.Instant;

/**
 * Thrown when a promo code has passed its expiration date.
 *
 * <p>This exception is thrown when a customer applies a promo code that
 * is past its valid end date. It uses the
 * {@link ErrorCode#PROMO_CODE_EXPIRED} code and HTTP 400 Bad Request
 * status. The code can no longer be used for any orders.</p>
 *
 * <p>The detail message includes the promo code and expiry timestamp
 * (e.g., "Promo code 'SUMMER2026' expired on 2026-08-31T23:59:59Z.")
 * to help the client understand when the code became invalid. The client
 * should look for a different active promo code.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#PROMO_CODE_EXPIRED
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
