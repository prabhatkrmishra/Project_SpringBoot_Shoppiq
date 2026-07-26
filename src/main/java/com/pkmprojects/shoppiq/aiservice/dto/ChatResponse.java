package com.pkmprojects.shoppiq.aiservice.dto;

import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;

import java.util.List;

/**
 * Response DTO returned after sending a message to the AI assistant.
 *
 * <p>
 * Contains the conversation ID (for subsequent messages), the full
 * message history, and the current conversation status so the frontend
 * can react to state changes (e.g. auto-resolution).
 *
 * @param chatId   the conversation identifier (e.g., {@code CHAT-2026-07-A3F2})
 * @param messages the full list of messages in chronological order
 * @param status   the current conversation status ({@code ACTIVE} or {@code RESOLVED})
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record ChatResponse(
        String chatId,
        List<ChatMessageDto> messages,
        ConversationStatus status
) {
}
