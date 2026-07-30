package com.pkmprojects.shoppiq.exception.general.seller;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when an approval or rejection action is attempted on a seller not in the expected status.
 *
 * <p>This exception is thrown when an administrator attempts to approve,
 * reject, suspend, or unsuspend a seller whose current status does not
 * permit the requested operation. It uses the {@link ErrorCode#INVALID_OPERATION}
 * code and HTTP 400 Bad Request status. The exception provides specific
 * factory methods for each invalid status transition scenario.</p>
 *
 * <p>The detail message includes the seller ID and the expected status
 * (e.g., "Seller '42' is not in PENDING status.") to help the client
 * understand which status transition was invalid. The administrator
 * should verify the seller's current status before retrying.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#INVALID_OPERATION
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
