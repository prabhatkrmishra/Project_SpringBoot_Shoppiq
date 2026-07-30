package com.pkmprojects.shoppiq.aiservice.dto.admin;

import java.time.Instant;

/**
 * Admin DTO for listing all AI chat conversations in the management dashboard.
 *
 * <p>This immutable record provides a paginated overview of AI chat
 * conversations for the admin monitoring panel. It includes user
 * identification fields that are not exposed in the customer-facing
 * API, such as user ID, display name, and email address. Guest
 * conversations are identified by "Guest" as the display name with
 * null user ID and email fields.</p>
 *
 * <p>The {@code messageCount} field is populated via batch counting
 * queries to avoid N+1 performance issues when rendering large
 * conversation lists. The {@code lastActivityAt} timestamp reflects
 * the most recent message or conversation update, enabling sorting
 * by recent activity.</p>
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
 * @author prabhatkrmishra
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
