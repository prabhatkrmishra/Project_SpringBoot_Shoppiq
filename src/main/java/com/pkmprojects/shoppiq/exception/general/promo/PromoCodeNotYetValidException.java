package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

import java.time.Instant;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a promo code
 * is not yet valid.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) for temporal validation (validFrom check).</p>
 *
 * @author prabhatkrmishra
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
