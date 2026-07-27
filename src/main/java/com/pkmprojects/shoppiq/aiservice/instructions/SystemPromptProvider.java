package com.pkmprojects.shoppiq.aiservice.instructions;

import com.pkmprojects.shoppiq.entity.user.User;

/**
 * Provides a system prompt for an AI chat conversation.
 *
 * <p>
 * Implementations supply the behavioral instructions that shape how the
 * AI assistant responds. Separate providers handle authenticated users
 * (full feature access) and guests (product discovery only).
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface SystemPromptProvider {

    /**
     * Builds the system prompt for a conversation.
     *
     * @param chatId the conversation's public identifier
     * @param user   the user (maybe {@code null} for guest conversations)
     * @return the system prompt text
     */
    String buildPrompt(String chatId, User user);
}
