package com.pkmprojects.shoppiq.enums;

/**
 * Verification states of a seller account.
 *
 * @author PrabhatKrMishra
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
