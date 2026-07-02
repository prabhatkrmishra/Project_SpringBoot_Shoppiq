package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when an approval or rejection action is attempted
 * on a seller not in PENDING status.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class SellerApprovalInvalidException extends InvalidOperationException {

    private SellerApprovalInvalidException(String detail) {
        super(ErrorCode.INVALID_OPERATION, detail);
    }

    public static SellerApprovalInvalidException notPending(Long sellerId) {
        return new SellerApprovalInvalidException(
                "Seller '%d' is not in PENDING status.".formatted(sellerId));
    }

    public static SellerApprovalInvalidException notActive(Long sellerId) {
        return new SellerApprovalInvalidException(
                "Seller '%d' is not ACTIVE and cannot be suspended.".formatted(sellerId));
    }

    public static SellerApprovalInvalidException notSuspended(Long sellerId) {
        return new SellerApprovalInvalidException(
                "Seller '%d' is not SUSPENDED and cannot be unsuspended.".formatted(sellerId));
    }
}
