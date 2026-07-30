package com.pkmprojects.shoppiq.aiservice.dto.admin;

import com.pkmprojects.shoppiq.aiservice.dto.ChatMessageDto;

import java.time.Instant;
import java.util.List;

/**
 * Admin DTO for viewing the full detail of a single AI chat conversation.
 *
 * <p>This immutable record contains the complete message history of a
 * conversation along with all metadata fields needed for administrative
 * review. It extends the summary data with a full chronological list of
 * {@link ChatMessageDto} objects, each carrying role labels, tool names,
 * and creation timestamps.</p>
 *
 * <p>The {@code resolvedAt} field is nullable and populated only when the
 * conversation has been resolved, either by the user, the system
 * auto-resolve task, or an admin action. Admins use this DTO to inspect
 * conversation content, verify AI responses, and audit tool usage.</p>
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
 * @author prabhatkrmishra
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
