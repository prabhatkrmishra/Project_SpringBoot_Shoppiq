package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.enums.CouponType;
import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a promo code's cart composition constraint is not met.
 *
 * <p>This exception is thrown when a promo code requires specific item
 * types or minimum quantities that the customer's cart does not satisfy.
 * It uses the {@link ErrorCode#PROMO_CODE_CART_CONSTRAINT} code and
 * HTTP 400 Bad Request status. The exception provides specific factory
 * methods for each constraint type.</p>
 *
 * <p>The detail message explains the specific constraint (e.g.,
 * "Promo code 'BULK20' is a bulk coupon and requires at least one
 * cart item with quantity greater than 1.") to help the client
 * understand what cart modification is needed.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#PROMO_CODE_CART_CONSTRAINT
 * @since 1.4.0
 */
public final class PromoCodeCartConstraintException extends InvalidOperationException {

    private PromoCodeCartConstraintException(String detail) {
        super(ErrorCode.PROMO_CODE_CART_CONSTRAINT, detail);
    }

    /**
     * Creates an exception for a {@link CouponType#SINGLE} code
     * applied to a cart with multi-unit items.
     */
    public static PromoCodeCartConstraintException singleCodeRequiresSingleUnitCart(String code) {
        return new PromoCodeCartConstraintException(
                "Promo code '%s' is a single-item coupon and requires all cart items to have a quantity of 1."
                        .formatted(code)
        );
    }

    /**
     * Creates an exception for a {@link CouponType#BULK} code
     * applied to a cart where all items have quantity == 1.
     */
    public static PromoCodeCartConstraintException bulkCodeRequiresMultiUnitCart(String code) {
        return new PromoCodeCartConstraintException(
                "Promo code '%s' is a bulk coupon and requires at least one cart item with quantity greater than 1."
                        .formatted(code)
        );
    }

    /**
     * Creates an exception for a code whose minimum item quantity
     * threshold is not met.
     */
    public static PromoCodeCartConstraintException minQuantityNotMet(String code, int required, int actual) {
        return new PromoCodeCartConstraintException(
                "Promo code '%s' requires a minimum quantity of %d units per item, but the highest quantity in your cart is %d."
                        .formatted(code, required, actual)
        );
    }
}
