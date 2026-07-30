package com.pkmprojects.shoppiq.enums;

/**
 * Publishing states of a product in the marketplace.
 *
 * <p>This enum models the product review and publication workflow. New
 * products start as {@link #DRAFT} until reviewed and published by an
 * admin. Only {@link #PUBLISHED} items are visible to customers in the
 * catalog and search results. {@link #REJECTED} items are hidden from
 * customers and cannot be purchased.</p>
 *
 * <p>The publishing status is independent of the product's stock level
 * or pricing. A product can be in stock but not yet published, or
 * published but out of stock. Both conditions affect customer
 * visibility differently.</p>
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
