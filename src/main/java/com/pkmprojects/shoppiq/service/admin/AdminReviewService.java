package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminReviewResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;

/**
 * Business contract for admin review moderation.
 *
 * <p>Defines operations for retrieving, approving, rejecting, and
 * deleting product reviews.</p>
 *
 * @author prabhatkrmishra
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
}
