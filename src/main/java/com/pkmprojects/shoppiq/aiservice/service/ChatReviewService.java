package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.entity.review.ItemReview;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Read-only review query facade for the AI chat assistant.
 *
 * <p>Decouples {@code ShoppiqTools} from {@code ItemReviewRepository},
 * providing a narrow, AI-specific query surface that returns
 * raw entities for text formatting in tool responses.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
public interface ChatReviewService {

    /**
     * Returns all reviews written by a user, most recent first.
     *
     * @param userId reviewer identifier
     * @return ordered list of reviews
     */
    List<ItemReview> findByUserNewestFirst(Long userId);
}
