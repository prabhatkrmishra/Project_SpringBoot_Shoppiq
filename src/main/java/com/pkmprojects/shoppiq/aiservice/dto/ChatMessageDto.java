package com.pkmprojects.shoppiq.aiservice.dto;

import java.time.Instant;

/**
 * Represents a single message within an AI chat conversation.
 *
 * <p>This immutable record carries the details of an individual message
 * including its role, textual content, and optional tool attribution.
 * It is used in conversation detail views, chat history retrieval, and
 * admin monitoring dashboards. Messages with a {@code TOOL} role include
 * the {@code toolName} field identifying which AI tool produced the
 * output (e.g., {@code search_products}, {@code get_order_status}).</p>
 *
 * <p>The {@code id} field uses synthetic numbering for guest messages
 * (which are not persisted to the database) and real database IDs for
 * authenticated conversation messages.</p>
 *
 * @param id        the message's database ID (or synthetic ID for guest messages)
 * @param role      the message role ({@code USER}, {@code ASSISTANT}, {@code SYSTEM}, {@code TOOL})
 * @param content   the textual content of the message
 * @param toolName  name of the tool that produced this message (nullable for non-tool messages)
 * @param createdAt timestamp when the message was created
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ChatMessageDto(
        Long id,
        String role,
        String content,
        String toolName,
        Instant createdAt
) {
}
