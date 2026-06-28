package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when attempting to create or update an item whose
 * unique business identifier already exists.
 *
 * <p>
 * Currently the uniqueness constraint is enforced on the SKU.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Represents duplicate item resources.</li>
 *     <li>Associates the failure with
 *     {@link ErrorCode#ITEM_ALREADY_EXISTS}.</li>
 *     <li>Provides expressive factory methods.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
                "Item review with user id %s already exists."
                        .formatted(userId)
        );
    }

    /**
     * Creates a generic duplicate item exception.
     *
     * @return duplicate item exception
     */
    public static DuplicateItemReviewException unknown() {
        return new DuplicateItemReviewException(
                "An item review of the user already exists."
        );
    }
}