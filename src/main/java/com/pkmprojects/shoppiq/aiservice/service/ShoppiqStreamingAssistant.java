package com.pkmprojects.shoppiq.aiservice.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * LangChain4j service interface for streaming (token-by-token) chat.
 *
 * <p>
 * Identical to {@link ShoppiqAssistant} but returns a {@link Flux} of response
 * tokens for real-time rendering. Requires the {@code langchain4j-reactor}
 * dependency (which provides {@code StreamingChatModel} support via Project Reactor).
 *
 * <p>
 * This interface is a plain type definition — no {@code @ChatService} or
 * {@code @SystemMessage} annotations are used. The system prompt is injected
 * via the builder's {@code .systemMessageProvider()} method.
 *
 * @author PrabhatKrMishra
 * @see ShoppiqAssistant
 * @since 1.0.0
 */
interface ShoppiqStreamingAssistant {

    /**
     * Sends a user message and returns the AI response as a reactive token stream.
     *
     * @param message the user's message text
     * @param chatId  the conversation identifier used as the memory ID
     * @return a {@link Flux} of response tokens
     */
    Flux<String> chat(@UserMessage String message, @MemoryId String chatId);
}
