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
 * Spring configuration that provides the LangChain4j {@link ChatMemoryProvider} bean
 * and memory lifecycle management.
 *
 * <p>
 * Each chat ID is mapped to a {@link MessageWindowChatMemory} instance that retains
 * the last {@value MAX_MESSAGES} messages. Instances are cached in a
 * {@link ConcurrentHashMap} so that memory persists across requests within the
 * same JVM — the same conversation will remember its full context.
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li>Memory is keyed by the public {@code chatId} string, not the database ID</li>
 *   <li>Guest and authenticated conversations share the same provider</li>
 *   <li>Memory is in-memory only; it is lost on JVM restart. The database stores
 *       the full message history separately for persistence across restarts</li>
 *   <li>The window size ({@value MAX_MESSAGES}) is hardcoded; future versions may
 *       make this configurable via {@code shoppiq.ai.max-messages}</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
     * <p>
     * Returns a cached {@link MessageWindowChatMemory} for each chat ID, creating
     * a new instance on first access. This ensures the AI model retains conversational
     * context across multiple requests within the same conversation.
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
     * <p>
     * Should be called when a conversation is resolved to free memory,
     * especially for guest sessions where memory is not persisted to the database.
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
