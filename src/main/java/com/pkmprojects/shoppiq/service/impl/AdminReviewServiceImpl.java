package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.admin.response.AdminReviewResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.ItemReview;
import com.pkmprojects.shoppiq.enums.ReviewStatus;
import com.pkmprojects.shoppiq.exception.ItemReviewNotFoundException;
import com.pkmprojects.shoppiq.repository.ItemReviewRepository;
import com.pkmprojects.shoppiq.service.admin.AdminReviewService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link AdminReviewService}.
 *
 * <p>
 * Provides review moderation operations for administrators
 * including retrieval and deletion.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Retrieve paginated reviews.</li>
 *     <li>Delete a review.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Uses constructor injection.</li>
 *     <li>Read operations use read-only transactions.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminReviewServiceImpl implements AdminReviewService {

    private final ItemReviewRepository itemReviewRepository;

    public AdminReviewServiceImpl(ItemReviewRepository itemReviewRepository) {
        this.itemReviewRepository = itemReviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminReviewResponse> getAllReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        var reviewPage = itemReviewRepository.findAll(pageable);

        return PageResponse.of(reviewPage, AdminReviewResponse::fromEntity);
    }

    @Override
    public void deleteReview(Long reviewId) {
        ItemReview review = itemReviewRepository.findById(reviewId)
                .orElseThrow(() -> ItemReviewNotFoundException.id(reviewId));

        itemReviewRepository.delete(review);
    }

    @Override
    public AdminReviewResponse approveReview(Long reviewId) {
        ItemReview review = itemReviewRepository.findById(reviewId)
                .orElseThrow(() -> ItemReviewNotFoundException.id(reviewId));

        review.setStatus(ReviewStatus.APPROVED);
        ItemReview saved = itemReviewRepository.save(review);
        return AdminReviewResponse.fromEntity(saved);
    }

    @Override
    public AdminReviewResponse rejectReview(Long reviewId) {
        ItemReview review = itemReviewRepository.findById(reviewId)
                .orElseThrow(() -> ItemReviewNotFoundException.id(reviewId));

        review.setStatus(ReviewStatus.REJECTED);
        ItemReview saved = itemReviewRepository.save(review);
        return AdminReviewResponse.fromEntity(saved);
    }
}