package com.pkmprojects.shoppiq.aiservice.dto;

import java.util.List;

/**
 * Response DTO returned after sending a message to the AI assistant.
 *
 * <p>
 * Contains the conversation ID (for subsequent messages) and the full
 * message history, allowing the frontend to render the complete conversation.
 *
 * @param chatId   the conversation identifier (e.g., {@code CHAT-2026-07-A3F2})
 * @param messages the full list of messages in chronological order
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record ChatResponse(
        String chatId,
        List<ChatMessageDto> messages
) {
}
