package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a user has
 * exceeded their per-user usage limit for a promo code.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) for per-user usage limit validation, separate from the global
 * usage limit.</p>
 *
 * @author prabhatkrmishra
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
