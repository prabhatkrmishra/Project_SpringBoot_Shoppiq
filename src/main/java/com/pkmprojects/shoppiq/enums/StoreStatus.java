package com.pkmprojects.shoppiq.enums;

/**
 * Lifecycle states of a seller's store.
 *
 * <p>This enum models the store lifecycle from creation through
 * publication. A new store is created as {@link #DRAFT} when a seller
 * is approved. The seller configures the store and sets it to
 * {@link #PUBLISHED} to go live. {@link #SUSPENDED} follows a seller
 * suspension and makes the store invisible to customers.</p>
 *
 * <p>The store status affects visibility: only PUBLISHED stores are
 * visible in the marketplace. DRAFT stores are only visible to the
 * seller and administrators during the setup process.</p>
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
