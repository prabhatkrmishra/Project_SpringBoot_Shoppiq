package com.pkmprojects.shoppiq.aiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for sending a message to the AI assistant.
 *
 * <p>This immutable record serves as the inbound contract for both the
 * authenticated and guest chat endpoints. The {@code message} field is
 * required and limited to 2000 characters to prevent excessive prompt
 * lengths. The {@code chatId} field is optional; when omitted, the
 * service creates a new conversation. The {@code model} field allows
 * the frontend to specify which LLM to use for the response, falling
 * back to the default model when not provided.</p>
 *
 * <p>Validation is performed via Jakarta Bean Validation annotations,
 * ensuring that malformed requests are rejected at the controller
 * layer before reaching the service.</p>
 *
 * @param message the user's message text (required, max 2000 characters)
 * @param chatId  the target conversation ID (optional; omit to create a new conversation)
 * @param model   the AI model to use (optional; omit to use the default model)
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ChatRequest(
        @NotBlank @Size(max = 2000) String message,
        String chatId,
        String model
) {
}
