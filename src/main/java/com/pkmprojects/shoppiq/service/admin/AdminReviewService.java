package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminReviewResponse;

import java.util.List;

/**
 * Business contract for admin review moderation.
 *
 * <p>
 * Defines the operations for moderating product reviews,
 * including retrieval and deletion.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Retrieve all reviews with pagination.</li>
 *     <li>Delete a review.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Works exclusively with DTOs.</li>
 *     <li>Implemented by {@code AdminReviewServiceImpl}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface AdminReviewService {

    /**
     * Retrieves all reviews with optional filtering.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return paginated review responses
     */
    PageResponse<AdminReviewResponse> getAllReviews(int page, int size);

    /**
     * Deletes a review.
     *
     * @param reviewId review identifier
     */
    void deleteReview(Long reviewId);

    /**
     * Approves a pending review, making it visible to customers.
     *
     * @param reviewId review identifier
     * @return updated review response
     */
    AdminReviewResponse approveReview(Long reviewId);

    /**
     * Rejects a pending review, hiding it from customers.
     *
     * @param reviewId review identifier
     * @return updated review response
     */
    AdminReviewResponse rejectReview(Long reviewId);

    /**
     * Page response wrapper.
     *
     * @param <T> content type
     */
    record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
    }
}