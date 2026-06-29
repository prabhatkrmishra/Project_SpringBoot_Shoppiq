package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.request.ItemReviewRequest;
import com.pkmprojects.shoppiq.dto.response.ItemReviewResponse;
import com.pkmprojects.shoppiq.entity.User;

import java.util.List;

/**
 * Service responsible for managing product reviews.
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
 * @author PrabhatKrMishra
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
     * Retrieves every review belonging to an item.
     *
     * @param itemId item identifier
     * @return ordered review list
     */
    List<ItemReviewResponse> getByItem(Long itemId);

    /**
     * Updates an existing review.
     *
     * @param reviewId review identifier
     * @param request  updated review
     * @return updated review
     */
    ItemReviewResponse update(
            Long reviewId,
            ItemReviewRequest request
    );

    /**
     * Deletes a review.
     *
     * @param reviewId review identifier
     */
    void delete(Long reviewId);
}