package com.pkmprojects.shoppiq.enums;

/**
 * Operational status of a seller account.
 *
 * <p>Independent of {@link VerificationStatus}. A seller may be
 * {@code ACTIVE} only after being {@code APPROVED}.
 * </p>
 *
 * @author PrabhatKrMishra
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
