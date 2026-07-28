package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

import java.math.BigDecimal;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when the order
 * subtotal does not meet the promo code's minimum amount.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) for minimum-order-amount validation.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class PromoCodeMinOrderAmountException extends InvalidOperationException {

    private PromoCodeMinOrderAmountException(String detail) {
        super(ErrorCode.PROMO_CODE_MIN_ORDER_AMOUNT_NOT_MET, detail);
    }

    /**
     * Creates an exception for a promo code minimum order amount not met.
     *
     * @param code     the promo code
     * @param required the minimum required subtotal
     * @param actual   the actual subtotal
     * @return a new exception instance
     */
    public static PromoCodeMinOrderAmountException forCode(String code, BigDecimal required, BigDecimal actual) {
        return new PromoCodeMinOrderAmountException(
                "Promo code '%s' requires a minimum order of %s. Your subtotal is %s."
                        .formatted(code, required, actual)
        );
    }
}
