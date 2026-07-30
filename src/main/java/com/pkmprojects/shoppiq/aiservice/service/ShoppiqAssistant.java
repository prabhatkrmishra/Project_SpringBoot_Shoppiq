package com.pkmprojects.shoppiq.aiservice.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

/**
 * LangChain4j service interface for synchronous (non-streaming) chat.
 *
 * <p>This package-private interface is used by LangChain4j's AiServices
 * builder to create dynamic proxies that handle synchronous AI chat
 * requests. The proxy automatically manages chat memory, system message
 * injection, tool execution, and RAG content retrieval based on the
 * builder configuration.</p>
 *
 * <p>The {@code @MemoryId} annotation on the chat ID parameter ensures
 * per-conversation memory isolation, preventing context from leaking
 * between different conversations.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
interface ShoppiqAssistant {

    /**
     * Sends a user message and returns the complete AI response.
     *
     * <p>The LangChain4j proxy intercepts this call and performs the full
     * AI pipeline: system message injection, chat memory retrieval, tool
     * execution, RAG content retrieval, and model inference. The complete
     * response text is returned after the model finishes generating.</p>
     *
     * @param message the user's message text
     * @param chatId  the conversation identifier used as the memory ID
     * @return the full AI response text
     */
    String chat(@UserMessage String message, @MemoryId String chatId);
}
