package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminReviewResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.enums.ReviewStatus;
import com.pkmprojects.shoppiq.exception.general.review.ItemReviewNotFoundException;
import com.pkmprojects.shoppiq.repository.item.ItemReviewRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <strong>Spring Boot Concept:</strong> Implementation of {@link AdminReviewService}
 * containing business logic for admin review moderation.
 *
 * <p>Provides paginated review listing, approval, rejection, and deletion for
 * moderating product reviews. Used by {@code AdminReviewController}.</p>
 *
 * <p>Why this design:
 * <ul>
 *   <li><strong>@Service</strong> — Spring stereotype for service-layer beans, auto-detected via component scanning.</li>
 *   <li><strong>@Transactional</strong> — Review moderation actions (approve/reject/delete) are atomic; reads use {@code readOnly = true}.</li>
 *   <li><strong>Constructor injection</strong> — final fields for immutability and testability.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see AdminReviewService
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminReviewServiceImpl implements AdminReviewService {

    private final ItemReviewRepository itemReviewRepository;

    public AdminReviewServiceImpl(ItemReviewRepository itemReviewRepository) {
        this.itemReviewRepository = itemReviewRepository;
    }

    /**
     * Retrieves a paginated list of all reviews sorted by creation date descending.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated review responses
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminReviewResponse> getAllReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        var reviewPage = itemReviewRepository.findAll(pageable);

        return PageResponse.of(reviewPage, AdminReviewResponse::fromEntity);
    }

    /**
     * Deletes a review by ID.
     *
     * @param reviewId review ID
     * @throws ItemReviewNotFoundException if the review does not exist
     */
    @Override
    public void deleteReview(Long reviewId) {
        ItemReview review = itemReviewRepository.findById(reviewId)
                .orElseThrow(() -> ItemReviewNotFoundException.id(reviewId));

        itemReviewRepository.delete(review);
    }

    /**
     * Approves a pending review — transitions status to APPROVED.
     *
     * @param reviewId review ID
     * @return updated review response
     * @throws ItemReviewNotFoundException if the review does not exist
     */
    @Override
    public AdminReviewResponse approveReview(Long reviewId) {
        ItemReview review = itemReviewRepository.findById(reviewId)
                .orElseThrow(() -> ItemReviewNotFoundException.id(reviewId));

        review.setStatus(ReviewStatus.APPROVED);
        ItemReview saved = itemReviewRepository.save(review);
        return AdminReviewResponse.fromEntity(saved);
    }

    /**
     * Rejects a pending review — transitions status to REJECTED.
     *
     * @param reviewId review ID
     * @return updated review response
     * @throws ItemReviewNotFoundException if the review does not exist
     */
    @Override
    public AdminReviewResponse rejectReview(Long reviewId) {
        ItemReview review = itemReviewRepository.findById(reviewId)
                .orElseThrow(() -> ItemReviewNotFoundException.id(reviewId));

        review.setStatus(ReviewStatus.REJECTED);
        ItemReview saved = itemReviewRepository.save(review);
        return AdminReviewResponse.fromEntity(saved);
    }
}
