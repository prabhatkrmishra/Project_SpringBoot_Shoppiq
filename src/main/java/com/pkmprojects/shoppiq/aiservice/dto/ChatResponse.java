package com.pkmprojects.shoppiq.aiservice.dto;

import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;

import java.util.List;

/**
 * Response DTO returned after sending a message to the AI assistant.
 *
 * <p>This immutable record contains the complete state of a conversation
 * after a message has been processed. It includes the public conversation
 * identifier, the full chronological message history (suitable for
 * rendering in the chat UI), and the current conversation status. The
 * frontend uses the status field to determine whether additional messages
 * can be sent or whether the conversation has been resolved.</p>
 *
 * <p>The messages list includes all roles (USER, ASSISTANT, SYSTEM, TOOL)
 * and is ordered chronologically, allowing the UI to reconstruct the
 * complete conversation thread with appropriate visual treatment for
 * each role.</p>
 *
 * @param chatId   the conversation identifier (e.g., {@code CHAT-2026-07-A3F2})
 * @param messages the full list of messages in chronological order
 * @param status   the current conversation status ({@code ACTIVE} or {@code RESOLVED})
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ChatResponse(
        String chatId,
        List<ChatMessageDto> messages,
        ConversationStatus status
) {
}
