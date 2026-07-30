package com.pkmprojects.shoppiq.service.item;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.review.ItemReviewRequest;
import com.pkmprojects.shoppiq.dto.review.ItemReviewResponse;
import com.pkmprojects.shoppiq.entity.user.User;

import java.util.List;

/**
 * Business contract for product review management.
 *
 * <p>Defines operations for creating, retrieving, updating, and deleting reviews
 * with business rules for seller/admin prevention, duplicate enforcement, and
 * visibility filtering.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface ItemReviewService {

    /**
     * Creates a new review for an item.
     *
     * @param itemId      item identifier
     * @param currentUser reviewer identifier
     * @param request     review request
     * @return created review
     */
    ItemReviewResponse create(
            Long itemId,
            User currentUser,
            ItemReviewRequest request
    );

    /**
     * Retrieves a review by its identifier.
     *
     * @param reviewId review identifier
     * @return matching review
     */
    ItemReviewResponse getById(Long reviewId);

    /**
     * Retrieves every review written by the given user.
     *
     * @param user reviewer
     * @return ordered review list
     */
    List<ItemReviewResponse> getByUser(User user);

    /**
     * Retrieves every review written by the given user, paginated.
     *
     * @param user reviewer
     * @param page page number (0-based)
     * @param size page size
     * @return paginated review list
     */
    PageResponse<ItemReviewResponse> getByUser(User user, int page, int size);

    /**
     * Retrieves every review belonging to an item visible to the
     * given user. Returns APPROVED reviews plus the user's own
     * PENDING/REJECTED reviews.
     *
     * @param itemId      item identifier
     * @param currentUser current user (maybe null for anonymous)
     * @return ordered review list
     */
    List<ItemReviewResponse> getByItemForUser(Long itemId, User currentUser);

    /**
     * Retrieves every review belonging to an item visible to the
     * given user, paginated.
     *
     * @param itemId      item identifier
     * @param currentUser current user (maybe null for anonymous)
     * @param page        page number (0-based)
     * @param size        page size
     * @return paginated review list
     */
    PageResponse<ItemReviewResponse> getByItemForUser(Long itemId, User currentUser, int page, int size);

    /**
     * Updates an existing review.
     *
     * <p>
     * Only the review's author or an administrator may perform this
     * operation.
     * </p>
     *
     * @param reviewId    review identifier
     * @param currentUser caller attempting the update
     * @param request     updated review
     * @return updated review
     */
    ItemReviewResponse update(
            Long reviewId,
            User currentUser,
            ItemReviewRequest request
    );

    /**
     * Deletes a review.
     *
     * <p>
     * Only the review's author or an administrator may perform this
     * operation.
     * </p>
     *
     * @param reviewId    review identifier
     * @param currentUser caller attempting the deletion
     */
    void delete(Long reviewId, User currentUser);
}
