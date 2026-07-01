package com.pkmprojects.shoppiq.enums;

/**
 * Lifecycle states of a seller's store.
 *
 * @author PrabhatKrMishra
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
