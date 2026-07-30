package com.pkmprojects.shoppiq.exception.general.seller;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a seller operation is attempted before the seller has been verified.
 *
 * <p>This exception is thrown when an unverified seller attempts to perform
 * actions that require admin approval. Sellers must be verified by an
 * administrator before they can list products or receive orders. It uses
 * the {@link ErrorCode#SELLER_NOT_VERIFIED} code and HTTP 400 Bad
 * Request status.</p>
 *
 * <p>The detail message includes the seller ID and the attempted action
 * (e.g., "Seller '42' is not verified. Cannot perform action: list_product.")
 * to help the client understand what operation was blocked. The seller
 * should wait for admin approval or contact support.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#SELLER_NOT_VERIFIED
 * @since 1.0.0
 */
public final class SellerNotVerifiedException extends InvalidOperationException {

    private SellerNotVerifiedException(String detail) {
        super(ErrorCode.SELLER_NOT_VERIFIED, detail);
    }

    /**
     * Creates an exception for an action attempted on an unverified seller.
     *
     * @param sellerId the seller ID
     * @param action   the attempted action
     * @return a new exception instance
     */
    public static SellerNotVerifiedException forAction(Long sellerId, String action) {
        return new SellerNotVerifiedException(
                "Seller '%d' is not verified. Cannot perform action: %s."
                        .formatted(sellerId, action));
    }
}
