package com.pkmprojects.shoppiq.aiservice.dto.admin;

import java.time.Instant;

/**
 * Admin DTO for listing all AI chat conversations in the management dashboard.
 *
 * <p>
 * Returned by the {@code GET /api/admin/ai-chats} endpoint as a paginated list.
 * Includes user identification fields to display who owns each conversation,
 * with guest conversations showing "Guest" as the username.
 *
 * @param chatId         the public conversation identifier
 * @param userId         the owning user's database ID ({@code null} for guests)
 * @param userName       the user's display name, or "Guest" for unauthenticated sessions
 * @param userEmail      the user's email address ({@code null} for guests)
 * @param title          auto-generated title from the first user message
 * @param status         current status ({@code ACTIVE} or {@code RESOLVED})
 * @param messageCount   number of user messages in the conversation
 * @param createdAt      timestamp when the conversation was created
 * @param lastActivityAt timestamp of the most recent message or update
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record AiChatLogDto(
        String chatId,
        Long userId,
        String userName,
        String userEmail,
        String title,
        String status,
        int messageCount,
        Instant createdAt,
        Instant lastActivityAt
) {
}
