package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a user has exceeded their per-user usage limit for a promo code.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class PromoCodeUserUsageLimitExceededException extends InvalidOperationException {

    private PromoCodeUserUsageLimitExceededException(String detail) {
        super(ErrorCode.PROMO_CODE_USER_USAGE_LIMIT_EXCEEDED, detail);
    }

    public static PromoCodeUserUsageLimitExceededException forCode(String code, int limit) {
        return new PromoCodeUserUsageLimitExceededException(
                "You have already used promo code '%s' the maximum of %d time(s)."
                        .formatted(code, limit)
        );
    }
}
