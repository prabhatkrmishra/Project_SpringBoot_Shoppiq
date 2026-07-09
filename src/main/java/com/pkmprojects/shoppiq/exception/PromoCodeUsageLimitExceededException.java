package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a promo code's global usage limit has been reached.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class PromoCodeUsageLimitExceededException extends InvalidOperationException {

    private PromoCodeUsageLimitExceededException(String detail) {
        super(ErrorCode.PROMO_CODE_USAGE_LIMIT_EXCEEDED, detail);
    }

    public static PromoCodeUsageLimitExceededException forCode(String code, int limit) {
        return new PromoCodeUsageLimitExceededException(
                "Promo code '%s' has reached its usage limit of %d.".formatted(code, limit)
        );
    }
}
