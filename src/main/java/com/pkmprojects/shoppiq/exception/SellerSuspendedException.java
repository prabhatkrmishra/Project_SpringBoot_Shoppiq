package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a seller operation is attempted
 * while the seller account is suspended.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class SellerSuspendedException extends InvalidOperationException {

    public SellerSuspendedException(String detail) {
        super(ErrorCode.SELLER_SUSPENDED, detail);
    }

    public static SellerSuspendedException forAction(Long sellerId, String action) {
        return new SellerSuspendedException(
                "Seller '%d' is suspended. Cannot perform action: %s."
                        .formatted(sellerId, action));
    }
}
