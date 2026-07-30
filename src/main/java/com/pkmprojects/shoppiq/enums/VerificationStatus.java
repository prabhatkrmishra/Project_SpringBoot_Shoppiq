package com.pkmprojects.shoppiq.enums;

/**
 * Verification states of a seller account.
 *
 * <p>This enum represents the one-time administrative review process for
 * new seller applications. Sellers start as {@link #PENDING} after
 * submitting their registration, then are {@link #APPROVED} or
 * {@link #REJECTED} after admin review. This status is independent of
 * the {@link SellerStatus} and only tracks the initial approval process.</p>
 *
 * <p>Only APPROVED sellers can list products and receive orders. PENDING
 * sellers are waiting for admin review, and REJECTED sellers cannot
 * operate on the platform.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum VerificationStatus {

    /**
     * Seller has applied but admin has not yet reviewed.
     */
    PENDING,

    /**
     * Seller has been approved by admin.
     */
    APPROVED,

    /**
     * Seller application has been rejected.
     */
    REJECTED
}
