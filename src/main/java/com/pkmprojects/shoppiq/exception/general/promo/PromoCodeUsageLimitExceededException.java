package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a promo code's global usage limit has been reached.
 *
 * <p>This exception is thrown when the total number of times a promo code
 * has been used across all customers equals the configured maximum. No
 * further uses are allowed. It uses the
 * {@link ErrorCode#PROMO_CODE_USAGE_LIMIT_EXCEEDED} code and HTTP 400
 * Bad Request status.</p>
 *
 * <p>The detail message includes the promo code and limit (e.g.,
 * "Promo code 'SUMMER2026' has reached its usage limit of 100.") to
 * help the client understand why the code was rejected. The client
 * should use a different promo code.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#PROMO_CODE_USAGE_LIMIT_EXCEEDED
 * @since 1.0.0
 */
public final class PromoCodeUsageLimitExceededException extends InvalidOperationException {

    private PromoCodeUsageLimitExceededException(String detail) {
        super(ErrorCode.PROMO_CODE_USAGE_LIMIT_EXCEEDED, detail);
    }

    /**
     * Creates an exception for a promo code that has reached its usage limit.
     *
     * @param code  the promo code
     * @param limit the global usage limit
     * @return a new exception instance
     */
    public static PromoCodeUsageLimitExceededException forCode(String code, int limit) {
        return new PromoCodeUsageLimitExceededException(
                "Promo code '%s' has reached its usage limit of %d.".formatted(code, limit)
        );
    }
}
