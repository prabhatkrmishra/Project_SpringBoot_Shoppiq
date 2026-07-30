package com.pkmprojects.shoppiq.aiservice.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides per-conversation chat memory using a sliding window of recent messages.
 *
 * <p>This configuration class manages the lifecycle of LangChain4j chat memory
 * instances that enable the AI model to maintain conversational context across
 * multiple requests within the same conversation. Each conversation is assigned
 * an independent memory window, ensuring that messages from one conversation do
 * not leak into another.</p>
 *
 * <p>Memory instances are stored in a thread-safe {@link ConcurrentHashMap} and
 * lazily created on first access for each chat ID. The sliding window retains
 * the most recent 20 messages per conversation, providing sufficient context
 * for the AI model while bounding memory consumption. When a conversation is
 * resolved, its cached memory is explicitly evicted to free resources.</p>
 *
 * <p>This configuration is conditionally enabled via the {@code shoppiq.ai.enabled}
 * property. When disabled, no memory beans are created and the AI service layer
 * is entirely omitted from the application context.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "shoppiq.ai.enabled", havingValue = "true", matchIfMissing = false)
public class ChatMemoryConfig {

    private static final Logger log = LoggerFactory.getLogger(ChatMemoryConfig.class);

    /**
     * Maximum number of messages retained in the sliding window per conversation.
     */
    private static final int MAX_MESSAGES = 20;

    /**
     * Cache of chat memory instances keyed by conversation chat ID.
     */
    private final ConcurrentHashMap<String, ChatMemory> memoryCache = new ConcurrentHashMap<>();

    @PostConstruct
    void logInit() {
        log.debug("[AI-INIT] ChatMemoryConfig initialised — maxMessages={}, cache={}",
                MAX_MESSAGES, memoryCache.getClass().getSimpleName());
    }

    /**
     * Creates a {@link ChatMemoryProvider} that supplies per-conversation memory windows.
     *
     * <p>The returned provider lazily initializes a {@link MessageWindowChatMemory}
     * instance for each unique chat ID encountered during conversations. Instances
     * are cached in a thread-safe map to ensure that repeated requests within the
     * same conversation share the same memory state. Each memory window retains
     * up to 20 messages, providing the AI model with sufficient conversational
     * context while preventing unbounded memory growth.</p>
     *
     * @return a provider that creates and caches {@link MessageWindowChatMemory} instances
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return chatId -> memoryCache.computeIfAbsent(String.valueOf(chatId),
                id -> MessageWindowChatMemory.builder()
                        .id(id)
                        .maxMessages(MAX_MESSAGES)
                        .build());
    }

    /**
     * Clears the cached chat memory for a specific conversation.
     *
     * <p>This method should be called when a conversation is resolved or when
     * a guest session is terminated. Evicting the memory entry ensures that
     * subsequent conversations with the same chat ID start with a fresh
     * context window, preventing stale conversational state from persisting
     * across session boundaries.</p>
     *
     * @param chatId the conversation's chat ID (e.g., {@code CHAT-2026-07-A3F2} or {@code guest-<uuid>})
     */
    public void clearMemory(String chatId) {
        ChatMemory removed = memoryCache.remove(String.valueOf(chatId));
        if (removed != null) {
            log.debug("[AI-MEMORY] Cleared memory for chatId={}", chatId);
        }
    }
}
