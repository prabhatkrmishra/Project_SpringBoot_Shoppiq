package com.pkmprojects.shoppiq.aiservice.dto.admin;

import com.pkmprojects.shoppiq.aiservice.dto.ChatMessageDto;

import java.time.Instant;
import java.util.List;

/**
 * Admin DTO for viewing the full detail of a single AI chat conversation.
 *
 * <p>
 * Returned by the {@code GET /api/admin/ai-chats/{chatId}} endpoint. Contains
 * the complete message history with role labels, tool names, and timestamps
 * for administrative review.
 *
 * @param chatId     the public conversation identifier
 * @param userId     the owning user's database ID ({@code null} for guests)
 * @param userName   the user's display name, or "Guest" for unauthenticated sessions
 * @param userEmail  the user's email address ({@code null} for guests)
 * @param title      auto-generated title from the first user message
 * @param status     current status ({@code ACTIVE} or {@code RESOLVED})
 * @param createdAt  timestamp when the conversation was created
 * @param resolvedAt timestamp when the conversation was resolved ({@code null} if still active)
 * @param messages   the full list of messages in chronological order
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record AiChatLogDetailDto(
        String chatId,
        Long userId,
        String userName,
        String userEmail,
        String title,
        String status,
        Instant createdAt,
        Instant resolvedAt,
        List<ChatMessageDto> messages
) {
}
