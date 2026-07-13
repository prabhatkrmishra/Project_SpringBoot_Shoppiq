package com.pkmprojects.shoppiq.aiservice.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

/**
 * LangChain4j service interface for synchronous (non-streaming) chat.
 *
 * <p>
 * This interface is a plain type definition used by
 * {@link dev.langchain4j.service.AiServices#builder} to create a dynamic proxy
 * at runtime. It does <strong>not</strong> use {@code @ChatService} or
 * {@code @SystemMessage} annotations — the system prompt is provided via
 * {@code .systemMessageProvider()} on the builder to allow per-request customization.
 *
 * <p>
 * The {@code @MemoryId} parameter routes each call to the correct
 * {@link dev.langchain4j.memory.chat.ChatMemoryProvider} slot, enabling
*   per-conversation memory isolation.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
interface ShoppiqAssistant {

    /**
     * Sends a user message and returns the complete AI response.
     *
     * @param message the user's message text
     * @param chatId  the conversation identifier used as the memory ID
     * @return the full AI response text
     */
    String chat(@UserMessage String message, @MemoryId String chatId);
}
