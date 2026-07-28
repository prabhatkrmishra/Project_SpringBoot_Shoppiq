package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminReviewResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;

/**
 * <strong>Spring Boot Concept:</strong> Business contract for admin review moderation.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * Defines the <strong>Service layer</strong> contract for moderating product reviews.
 * Architecture: {@code AdminReviewController → AdminReviewService → ItemReviewRepository}.
 * </p>
 *
 * <h2>Business Logic Responsibilities</h2>
 * <ul>
 *     <li>Retrieve all reviews with pagination (sorted newest-first).</li>
 *     <li>Approve pending reviews — sets status to APPROVED, making them visible to customers.</li>
 *     <li>Reject pending reviews — sets status to REJECTED, hiding them from the product page.</li>
 *     <li>Delete inappropriate reviews entirely.</li>
 * </ul>
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
