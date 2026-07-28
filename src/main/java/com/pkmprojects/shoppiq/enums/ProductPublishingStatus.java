package com.pkmprojects.shoppiq.enums;

/**
 * <strong>Spring Boot Concept:</strong> Publishing states of a product in the marketplace.
 *
 * <p>New products created by a seller start as {@code DRAFT} until
 * reviewed and published by an admin.
 * </p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Admin moderation workflow</strong> — {@code DRAFT → PUBLISHED
 *         (or REJECTED)} defines a content approval pipeline. The
 *         {@code Item} entity defaults to {@code DRAFT} for new products,
 *         and only {@code PUBLISHED} items are visible to customers.</li>
 *     <li><strong>Stored as STRING</strong> — Used with
 *         {@code @Enumerated(EnumType.STRING)} in the JPA entity for
 *         readable database values.</li>
 *     <li><strong>Three-state model</strong> — DRAFT (initial), PUBLISHED
 *         (live), REJECTED (blocked). This is sufficient for a simple
 *         moderation flow without requiring a full workflow engine.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum ProductPublishingStatus {

    /**
     * Product is not yet visible to customers.
     */
    DRAFT,

    /**
     * Product is live and visible to customers.
     */
    PUBLISHED,

    /**
     * Product was rejected by admin during review.
     */
    REJECTED
}
