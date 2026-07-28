package com.pkmprojects.shoppiq.enums;

/**
 * <strong>Spring Boot Concept:</strong> Lifecycle states of a seller's store.
 *
 * <p>A new store is created as {@link #DRAFT} when a seller is approved.
 * The seller completes the profile and sets it to {@link #PUBLISHED}.
 * {@link #SUSPENDED} follows a seller suspension.</p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Store publishing workflow</strong> — DRAFT allows sellers
 *         to prepare their storefront before going live. PUBLISHED makes it
 *         visible to customers. SUSPENDED is triggered by admin action.</li>
 *     <li><strong>Stored as STRING</strong> — Used with
 *         {@code @Enumerated(EnumType.STRING)} in the JPA entity for
 *         readable database values.</li>
 *     <li><strong>Service-layer cascade</strong> — When a seller is suspended,
 *         the store status transitions to SUSPENDED via service-layer logic,
 *         not a database trigger. This keeps business rules in the
 *         application layer.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum StoreStatus {

    /**
     * Store is created but not yet published by the seller.
     */
    DRAFT,

    /**
     * Store is live and visible to customers.
     */
    PUBLISHED,

    /**
     * Store has been suspended (typically follows seller suspension).
     */
    SUSPENDED
}
