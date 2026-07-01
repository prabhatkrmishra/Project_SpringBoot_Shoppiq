package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a seller operation is attempted
 * before the seller has been verified.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class SellerNotVerifiedException extends InvalidOperationException {

    public SellerNotVerifiedException(String detail) {
        super(ErrorCode.SELLER_NOT_VERIFIED, detail);
    }

    public static SellerNotVerifiedException forAction(Long sellerId, String action) {
        return new SellerNotVerifiedException(
                "Seller '%d' is not verified. Cannot perform action: %s."
                        .formatted(sellerId, action));
    }
}
