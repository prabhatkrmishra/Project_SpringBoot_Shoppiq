package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a promo code is currently inactive.
 *
 * <p>This exception is thrown when a customer applies a promo code that
 * has been deactivated by an administrator. It uses the
 * {@link ErrorCode#PROMO_CODE_INACTIVE} code and HTTP 400 Bad Request
 * status. The code exists but is not currently usable.</p>
 *
 * <p>The detail message includes the promo code (e.g.,
 * "Promo code 'SUMMER2026' is currently inactive.") to help the client
 * understand which code was deactivated. The client should contact
 * support or use a different promo code.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#PROMO_CODE_INACTIVE
 * @since 1.0.0
 */
public final class PromoCodeInactiveException extends InvalidOperationException {

    private PromoCodeInactiveException(String detail) {
        super(ErrorCode.PROMO_CODE_INACTIVE, detail);
    }

    /**
     * Creates an exception for an inactive promo code.
     *
     * @param code the promo code
     * @return a new exception instance
     */
    public static PromoCodeInactiveException forCode(String code) {
        return new PromoCodeInactiveException(
                "Promo code '%s' is currently inactive.".formatted(code)
        );
    }
}
