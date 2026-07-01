package com.pkmprojects.shoppiq.enums;

/**
 * Publishing states of a product in the marketplace.
 *
 * <p>New products created by a seller start as {@code DRAFT} until
 * reviewed and published by an admin.
 * </p>
 *
 * @author PrabhatKrMishra
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
