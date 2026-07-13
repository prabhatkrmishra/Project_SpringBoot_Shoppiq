package com.pkmprojects.shoppiq.aiservice.dto;

import java.time.Instant;

/**
 * Summary DTO for listing a user's AI chat conversations.
 *
 * <p>
 * Returned by the {@code GET /api/ai/chat/conversations} endpoint. Contains
 * enough information for the sidebar conversation list without loading full
 * message histories.
 *
 * @param chatId        the public conversation identifier
 * @param title         auto-generated title from the first user message
 * @param status        current status ({@code ACTIVE} or {@code RESOLVED})
 * @param messageCount  number of user messages in the conversation
 * @param createdAt     timestamp when the conversation was created
 * @param lastMessageAt timestamp of the most recent message update
 * @author PrabhatKrMishra
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
