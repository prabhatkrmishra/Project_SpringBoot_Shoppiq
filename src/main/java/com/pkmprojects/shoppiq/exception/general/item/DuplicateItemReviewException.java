package com.pkmprojects.shoppiq.exception.general.item;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when attempting to
 * create a review for a product that the user has already reviewed.
 *
 * <p>Leaf exception in the duplicate-resource hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.DuplicateResourceException}
 * (HTTP 409) to enforce the one-review-per-user-per-item business rule.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class DuplicateItemReviewException
        extends DuplicateResourceException {

    /**
     * Creates a duplicate item review exception.
     *
     * @param detail detailed description
     */
    private DuplicateItemReviewException(String detail) {
        super(ErrorCode.ITEM_REVIEW_ALREADY_EXISTS, detail);
    }

    /**
     * Creates an exception indicating that the item review with
     * the user id already exists.
     *
     * @param userId duplicate user id
     * @return duplicate item review exception
     */
    public static DuplicateItemReviewException userId(Long userId) {
        return new DuplicateItemReviewException(
                "Item review with user id '%d' already exists."
                        .formatted(userId)
        );
    }

    /**
     * Creates a generic duplicate item review exception.
     *
     * @return duplicate item review exception
     */
    public static DuplicateItemReviewException unknown() {
        return new DuplicateItemReviewException(
                "An item review of the user already exists."
        );
    }
}
