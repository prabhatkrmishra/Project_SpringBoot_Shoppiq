package com.pkmprojects.shoppiq.exception.general.seller;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a seller operation is attempted while the seller account is suspended.
 *
 * <p>This exception is thrown when a suspended seller attempts to perform
 * actions that require an active account. Suspended sellers cannot list
 * products, receive orders, or access restricted functionality until
 * reactivated by an administrator. It uses the
 * {@link ErrorCode#SELLER_SUSPENDED} code and HTTP 400 Bad Request
 * status.</p>
 *
 * <p>The detail message includes the seller ID and the attempted action
 * (e.g., "Seller '42' is suspended. Cannot perform action: list_product.")
 * to help the client understand what operation was blocked. The seller
 * should contact an administrator to resolve the suspension.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#SELLER_SUSPENDED
 * @since 1.0.0
 */
public final class SellerSuspendedException extends InvalidOperationException {

    private SellerSuspendedException(String detail) {
        super(ErrorCode.SELLER_SUSPENDED, detail);
    }

    /**
     * Creates an exception for an action attempted on a suspended seller.
     *
     * @param sellerId the seller ID
     * @param action   the attempted action
     * @return a new exception instance
     */
    public static SellerSuspendedException forAction(Long sellerId, String action) {
        return new SellerSuspendedException(
                "Seller '%d' is suspended. Cannot perform action: %s."
                        .formatted(sellerId, action));
    }
}
