package com.pkmprojects.shoppiq.service.item;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.review.ItemReviewRequest;
import com.pkmprojects.shoppiq.dto.review.ItemReviewResponse;
import com.pkmprojects.shoppiq.entity.user.User;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Service responsible for managing product reviews.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * Defines the <strong>Service layer</strong> contract for product review operations.
 * Architecture: {@code ItemReviewController → ItemReviewService → ItemReviewRepository / ItemLookupService}.
 * </p>
 *
 * <h2>Business Logic Responsibilities</h2>
 * <ul>
 *     <li>Create reviews with business rules: no seller reviews, no admin reviews,
 *         one review per user per product.</li>
 *     <li>Retrieve reviews with visibility rules: APPROVED reviews for everyone,
 *         plus the user's own PENDING/REJECTED reviews.</li>
 *     <li>Update reviews — only the author or an admin can edit.</li>
 *     <li>Delete reviews — only the author or an admin can delete.</li>
 * </ul>
 *
 * <p>
 * Defines the business operations available for the Item Review module.
 * Implementations are responsible for validating business rules,
 * coordinating persistence operations and mapping between DTOs and entities.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Create reviews.</li>
 *     <li>Retrieve reviews.</li>
 *     <li>Update reviews.</li>
 *     <li>Delete reviews.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Operates entirely on DTOs.</li>
 *     <li>Does not expose persistence entities.</li>
 *     <li>Implemented by {@code ItemReviewServiceImpl}.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface ItemReviewService {

    /**
     * Creates a new review for an item.
     *
     * @param itemId  item identifier
     * @param currentUser  reviewer identifier
     * @param request review request
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
     * @param itemId item identifier
     * @param currentUser current user (may be null for anonymous)
     * @return ordered review list
     */
    List<ItemReviewResponse> getByItemForUser(Long itemId, User currentUser);

    /**
     * Retrieves every review belonging to an item visible to the
     * given user, paginated.
     *
     * @param itemId item identifier
     * @param currentUser current user (may be null for anonymous)
     * @param page page number (0-based)
     * @param size page size
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
