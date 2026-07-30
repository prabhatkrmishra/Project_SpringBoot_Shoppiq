package com.pkmprojects.shoppiq.exception.general.item;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when attempting to create a review for a product that the user has already reviewed.
 *
 * <p>This exception is thrown during review creation when the authenticated
 * user already has a review for the specified item. Only one review per
 * user per item is allowed. It uses the
 * {@link ErrorCode#ITEM_REVIEW_ALREADY_EXISTS} code and HTTP 409 Conflict
 * status.</p>
 *
 * <p>The detail message should explain that the user has already reviewed
 * this product and suggest editing the existing review instead. The client
 * should use the update endpoint to modify the existing review.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ITEM_REVIEW_ALREADY_EXISTS
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
