package com.pkmprojects.shoppiq.enums;

import com.pkmprojects.shoppiq.entity.review.ItemReview;

/**
 * Moderation status of an {@link ItemReview}.
 *
 * <p>This enum models the review moderation workflow. New reviews start
 * as {@link #PENDING} and become {@link #APPROVED} (visible to all
 * customers) or {@link #REJECTED} (hidden from customers) after admin
 * review. Only APPROVED reviews are displayed on product detail pages
 * and factored into average rating calculations.</p>
 *
 * <p>The moderation process ensures that reviews comply with the
 * platform's content guidelines before being published. Admins can
 * approve or reject reviews from the admin dashboard.</p>
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
