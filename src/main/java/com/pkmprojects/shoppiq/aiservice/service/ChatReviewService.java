package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.entity.review.ItemReview;

import java.util.List;

/**
 * Read-only review query facade for the AI chat assistant.
 *
 * <p>This interface provides a narrow, AI-specific query surface for
 * review data access. It decouples the AI tool methods from the
 * review repository, allowing the AI layer to query user reviews
 * without depending on the full review service API. This separation
 * ensures that AI tool invocations operate within read-only
 * transactional boundaries.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public interface ChatReviewService {

    /**
     * Returns all reviews written by a user, most recent first.
     *
     * <p>Used by the user reviews tool to display the authenticated user's
     * review history. Results are ordered by creation date descending.</p>
     *
     * @param userId reviewer identifier
     * @return ordered list of reviews
     */
    List<ItemReview> findByUserNewestFirst(Long userId);
}
