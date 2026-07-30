package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.repository.item.ItemReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-only implementation of {@link ChatReviewService}.
 *
 * <p>This service delegates to the {@link ItemReviewRepository} for all
 * review queries, providing a read-only transactional boundary for AI
 * tool data access. The {@code @Transactional(readOnly = true)}
 * annotation ensures that no data modifications occur through this
 * service.</p>
 *
 * @author prabhatkrmishra
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
