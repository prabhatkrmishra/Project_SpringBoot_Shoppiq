package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a promo code's
 * global usage limit has been reached.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) for usage-limit validation.</p>
 *
 * @author prabhatkrmishra
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
