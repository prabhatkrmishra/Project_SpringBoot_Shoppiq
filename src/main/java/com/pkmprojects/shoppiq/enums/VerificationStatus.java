package com.pkmprojects.shoppiq.enums;

/**
 * <strong>Spring Boot Concept:</strong> Verification states of a seller account.
 *
 * <p>Represents the one-time administrative review process. Sellers
 * start as {@link #PENDING} upon application. Admin either
 * {@link #APPROVED} (allows selling) or {@link #REJECTED} (blocks).</p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>One-time approval workflow</strong> — Unlike
 *         {@link SellerStatus} which can fluctuate, this status is
 *         effectively immutable after initial processing (a rejected seller
 *         may reapply, creating a new verification request).</li>
 *     <li><strong>Separated from operational status</strong> — Verification
 *         is an admin review gate; operational status handles ongoing
 *         account management. Separating these two concerns makes the
 *         domain model clearer.</li>
 *     <li><strong>Stored as STRING</strong> — Used with
 *         {@code @Enumerated(EnumType.STRING)} in the JPA entity.</li>
 * </ul>
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
