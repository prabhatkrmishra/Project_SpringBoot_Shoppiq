package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

import java.math.BigDecimal;

/**
 * Exception thrown when the order subtotal does not meet the promo code's minimum amount.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class PromoCodeMinOrderAmountException extends InvalidOperationException {

    private PromoCodeMinOrderAmountException(String detail) {
        super(ErrorCode.PROMO_CODE_MIN_ORDER_AMOUNT_NOT_MET, detail);
    }

    public static PromoCodeMinOrderAmountException forCode(String code, BigDecimal required, BigDecimal actual) {
        return new PromoCodeMinOrderAmountException(
                "Promo code '%s' requires a minimum order of %s. Your subtotal is %s."
                        .formatted(code, required, actual)
        );
    }
}
