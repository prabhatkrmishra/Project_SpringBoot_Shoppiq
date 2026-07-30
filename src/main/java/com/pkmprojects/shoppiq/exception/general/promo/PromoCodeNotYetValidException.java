package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

import java.time.Instant;

/**
 * Thrown when a promo code is not yet valid (before its start date).
 *
 * <p>This exception is thrown when a customer applies a promo code before
 * its valid start date. It uses the
 * {@link ErrorCode#PROMO_CODE_NOT_YET_VALID} code and HTTP 400 Bad
 * Request status. The code will become active on the specified date.</p>
 *
 * <p>The detail message includes the promo code and start date (e.g.,
 * "Promo code 'SUMMER2026' is not valid until 2026-08-01T00:00:00Z.")
 * to help the client understand when the code will become active. The
 * client should wait until the start date or use a different code.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#PROMO_CODE_NOT_YET_VALID
 * @since 1.0.0
 */
public final class PromoCodeNotYetValidException extends InvalidOperationException {

    private PromoCodeNotYetValidException(String detail) {
        super(ErrorCode.PROMO_CODE_NOT_YET_VALID, detail);
    }

    /**
     * Creates an exception for a promo code that is not yet valid.
     *
     * @param code      the promo code
     * @param validFrom the valid-from timestamp
     * @return a new exception instance
     */
    public static PromoCodeNotYetValidException forCode(String code, Instant validFrom) {
        return new PromoCodeNotYetValidException(
                "Promo code '%s' is not valid until %s.".formatted(code, validFrom)
        );
    }
}
