package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

import java.math.BigDecimal;

/**
 * Thrown when the order subtotal does not meet the promo code's minimum amount.
 *
 * <p>This exception is thrown when a customer's cart subtotal is below the
 * minimum order amount required by the promo code. It uses the
 * {@link ErrorCode#PROMO_CODE_MIN_ORDER_AMOUNT_NOT_MET} code and
 * HTTP 400 Bad Request status. The customer must add more items to
 * qualify.</p>
 *
 * <p>The detail message includes the promo code, required minimum, and
 * actual subtotal (e.g., "Promo code 'SUMMER2026' requires a minimum
 * order of $50.00. Your subtotal is $35.00.") to help the client
 * understand how much more they need to add.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#PROMO_CODE_MIN_ORDER_AMOUNT_NOT_MET
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
