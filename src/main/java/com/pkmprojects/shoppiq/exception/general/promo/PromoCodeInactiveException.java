package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a promo code
 * is inactive.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) for active-status validation.</p>
 *
 * @author prabhatkrmishra
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
