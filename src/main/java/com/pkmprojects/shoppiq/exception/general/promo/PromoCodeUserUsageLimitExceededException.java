package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a user has exceeded their per-user usage limit for a promo code.
 *
 * <p>This exception is thrown when an individual customer has used a promo
 * code the maximum number of times allowed per user. Other customers may
 * still be able to use it. It uses the
 * {@link ErrorCode#PROMO_CODE_USER_USAGE_LIMIT_EXCEEDED} code and
 * HTTP 400 Bad Request status.</p>
 *
 * <p>The detail message includes the promo code and per-user limit (e.g.,
 * "You have already used promo code 'SUMMER2026' the maximum of 3 time(s).")
 * to help the client understand why the code was rejected. The client
 * should use a different promo code.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#PROMO_CODE_USER_USAGE_LIMIT_EXCEEDED
 * @since 1.0.0
 */
public final class PromoCodeUserUsageLimitExceededException extends InvalidOperationException {

    private PromoCodeUserUsageLimitExceededException(String detail) {
        super(ErrorCode.PROMO_CODE_USER_USAGE_LIMIT_EXCEEDED, detail);
    }

    /**
     * Creates an exception for a user exceeding their per-user promo code limit.
     *
     * @param code  the promo code
     * @param limit the per-user usage limit
     * @return a new exception instance
     */
    public static PromoCodeUserUsageLimitExceededException forCode(String code, int limit) {
        return new PromoCodeUserUsageLimitExceededException(
                "You have already used promo code '%s' the maximum of %d time(s)."
                        .formatted(code, limit)
        );
    }
}
