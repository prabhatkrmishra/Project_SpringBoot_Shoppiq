package com.pkmprojects.shoppiq.aiservice.dto;

import java.time.Instant;

/**
 * Summary DTO for listing a user's AI chat conversations.
 *
 * <p>This immutable record provides a lightweight view of a conversation
 * suitable for rendering in the sidebar conversation list. It contains
 * only the fields needed for display without loading full message
 * histories, keeping the list endpoint performant even for users with
 * many conversations.</p>
 *
 * <p>The {@code title} field is auto-generated from the user's first
 * message (truncated to 50 characters). The {@code messageCount} field
 * represents the number of user messages (not total messages) and is
 * used to give a quick sense of conversation length.</p>
 *
 * @param chatId        the public conversation identifier
 * @param title         auto-generated title from the first user message
 * @param status        current status ({@code ACTIVE} or {@code RESOLVED})
 * @param messageCount  number of user messages in the conversation
 * @param createdAt     timestamp when the conversation was created
 * @param lastMessageAt timestamp of the most recent message update
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ConversationSummary(
        String chatId,
        String title,
        String status,
        int messageCount,
        Instant createdAt,
        Instant lastMessageAt
) {
}
