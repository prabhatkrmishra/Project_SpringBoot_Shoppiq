package com.pkmprojects.shoppiq.enums;

/**
 * Operational status of a seller account.
 *
 * <p>This enum is independent of {@link VerificationStatus} and tracks
 * whether a seller is currently allowed to operate on the platform.
 * A seller can be {@link #ACTIVE} (selling normally), {@link #SUSPENDED}
 * (blocked by admin from selling), or {@link #INACTIVE} (not yet approved
 * or voluntarily deregistered).</p>
 *
 * <p>The seller status affects what operations the seller can perform:
 * ACTIVE sellers can list products and receive orders, SUSPENDED sellers
 * cannot, and INACTIVE sellers have not completed the registration
 * flow.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum SellerStatus {

    /**
     * Seller is actively selling on the platform.
     */
    ACTIVE,

    /**
     * Seller has been suspended by admin.
     */
    SUSPENDED,

    /**
     * Seller account is inactive (e.g. not yet approved, or deregistered).
     */
    INACTIVE
}
