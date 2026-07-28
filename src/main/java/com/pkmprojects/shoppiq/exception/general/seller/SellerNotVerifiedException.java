package com.pkmprojects.shoppiq.exception.general.seller;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a seller
 * operation is attempted before the seller has been verified.
 *
 * <p>Leaf exception in the invalid-operation hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.InvalidOperationException}
 * (HTTP 400) to prevent operations on unverified sellers.</p>
 *
 * @author prabhatkrmishra
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
