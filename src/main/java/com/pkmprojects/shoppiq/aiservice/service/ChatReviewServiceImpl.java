package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.repository.item.ItemReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Read-only implementation of {@link ChatReviewService}.
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ChatReviewServiceImpl implements ChatReviewService {

    private final ItemReviewRepository reviewRepository;

    @Override
    public List<ItemReview> findByUserNewestFirst(Long userId) {
        return reviewRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }
}
