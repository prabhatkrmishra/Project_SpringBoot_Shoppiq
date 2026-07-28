package com.pkmprojects.shoppiq.exception.general.seller;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a seller
 * operation is attempted while the seller account is suspended.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) to prevent operations on suspended sellers.</p>
 *
 * @author prabhatkrmishra
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
