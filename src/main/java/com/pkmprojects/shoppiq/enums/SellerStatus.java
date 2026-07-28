package com.pkmprojects.shoppiq.enums;

/**
 * <strong>Spring Boot Concept:</strong> Operational status of a seller account.
 *
 * <p>Independent of {@link VerificationStatus}. A seller may be
 * {@code ACTIVE} only after being {@code APPROVED}.
 * </p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Operational lifecycle</strong> — Tracks whether a seller
 *         is currently {@code ACTIVE}, {@code SUSPENDED}, or
 *         {@code INACTIVE}. This status can change multiple times over
 *         the seller's lifetime (e.g., suspension for policy violations,
 *         reactivation after appeal).</li>
 *     <li><strong>Separated from verification</strong> — Unlike
 *         {@link VerificationStatus} (one-time approval), this status
 *         handles day-to-day operational state. The two enums are
 *         independent but interdependent: a seller must be APPROVED
 *         before becoming ACTIVE.</li>
 *     <li><strong>Stored as STRING</strong> — Used with
 *         {@code @Enumerated(EnumType.STRING)} in the JPA entity.</li>
 * </ul>
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
