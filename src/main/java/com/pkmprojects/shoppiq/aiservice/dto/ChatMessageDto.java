package com.pkmprojects.shoppiq.aiservice.dto;

import java.time.Instant;

/**
 * DTO representing a single message within an AI chat conversation.
 *
 * <p>
 * Used in both the conversation detail view (admin) and the user-facing
 * chat history. Tool messages include the {@code toolName} field to
 * identify which tool produced the output.
 *
 * @param id        the message's database ID
 * @param role      the message role ({@code USER}, {@code ASSISTANT}, {@code SYSTEM}, {@code TOOL})
 * @param content   the textual content of the message
 * @param toolName  name of the tool that produced this message (nullable for non-tool messages)
 * @param createdAt timestamp when the message was created
 * @author PrabhatKrMishra
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
