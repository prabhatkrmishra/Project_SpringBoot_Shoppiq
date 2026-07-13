package com.pkmprojects.shoppiq.aiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for sending a message to the AI assistant.
 *
 * <p>
 * Used by both the authenticated {@code /api/ai/chat} and guest
 * {@code /api/ai/guest} endpoints. The {@code chatId} field is
 * optional — when omitted, a new conversation is created.
 *
 * @param message the user's message (required, max 2000 characters)
 * @param chatId  the target conversation ID (optional; omit to create a new conversation)
 * @param model   the AI model to use (optional; omit to use the default model)
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record ChatRequest(
        @NotBlank @Size(max = 2000) String message,
        String chatId,
        String model
) {
}
