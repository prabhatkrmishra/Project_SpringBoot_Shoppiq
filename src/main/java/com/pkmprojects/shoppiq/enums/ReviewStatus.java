package com.pkmprojects.shoppiq.enums;

import com.pkmprojects.shoppiq.entity.review.ItemReview;

/**
 * <strong>Spring Boot Concept:</strong> Moderation status of an {@link ItemReview}.
 *
 * <p>Defines the lifecycle of a customer product review. New reviews
 * start as {@link #PENDING}, requiring admin approval before they
 * become visible. {@link #APPROVED} reviews are shown on the product
 * page, while {@link #REJECTED} reviews are hidden.</p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Content moderation workflow</strong> — A simple three-state
 *         model (PENDING → APPROVED/REJECTED) for admin review of
 *         user-generated content.</li>
 *     <li><strong>Stored as STRING</strong> — Used with
 *         {@code @Enumerated(EnumType.STRING)} in the JPA entity for
 *         readable database values and easier SQL queries.</li>
 *     <li><strong>Default PENDING</strong> — The {@code ItemReview} entity
 *         sets the default to {@code PENDING} via {@code @Builder.Default},
 *         ensuring all new reviews start in a moderation-required state.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum ReviewStatus {

    /**
     * Review is awaiting admin moderation.
     */
    PENDING,

    /**
     * Review has been approved and is visible to customers.
     */
    APPROVED,

    /**
     * Review has been rejected and is hidden from customers.
     */
    REJECTED
}
