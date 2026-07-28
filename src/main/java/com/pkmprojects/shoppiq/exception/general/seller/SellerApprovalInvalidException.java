package com.pkmprojects.shoppiq.exception.general.seller;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when an approval
 * or rejection action is attempted on a seller not in the expected status.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) for seller lifecycle state machine enforcement
 * (approve, reject, suspend, unsuspend).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class SellerApprovalInvalidException extends InvalidOperationException {

    private SellerApprovalInvalidException(String detail) {
        super(ErrorCode.INVALID_OPERATION, detail);
    }

    /**
     * Creates an exception for a seller that is not in PENDING status.
     *
     * @param sellerId the seller ID
     * @return a new exception instance
     */
    public static SellerApprovalInvalidException notPending(Long sellerId) {
        return new SellerApprovalInvalidException(
                "Seller '%d' is not in PENDING status.".formatted(sellerId));
    }

    /**
     * Creates an exception for a seller that is not ACTIVE and cannot be suspended.
     *
     * @param sellerId the seller ID
     * @return a new exception instance
     */
    public static SellerApprovalInvalidException notActive(Long sellerId) {
        return new SellerApprovalInvalidException(
                "Seller '%d' is not ACTIVE and cannot be suspended.".formatted(sellerId));
    }

    /**
     * Creates an exception for a seller that is not SUSPENDED and cannot be unsuspended.
     *
     * @param sellerId the seller ID
     * @return a new exception instance
     */
    public static SellerApprovalInvalidException notSuspended(Long sellerId) {
        return new SellerApprovalInvalidException(
                "Seller '%d' is not SUSPENDED and cannot be unsuspended.".formatted(sellerId));
    }
}
