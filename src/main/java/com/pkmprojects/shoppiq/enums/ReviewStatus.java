package com.pkmprojects.shoppiq.enums;

import com.pkmprojects.shoppiq.entity.review.ItemReview;

/**
 * Moderation status of an {@link ItemReview}.
 *
 * @author PrabhatKrMishra
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
