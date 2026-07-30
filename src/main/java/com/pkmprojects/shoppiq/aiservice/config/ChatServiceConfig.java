package com.pkmprojects.shoppiq.aiservice.config;

import com.pkmprojects.shoppiq.aiservice.instructions.SystemPromptProvider;
import com.pkmprojects.shoppiq.aiservice.repository.ChatConversationRepository;
import com.pkmprojects.shoppiq.aiservice.repository.ChatMessageRepository;
import com.pkmprojects.shoppiq.aiservice.service.ChatService;
import com.pkmprojects.shoppiq.aiservice.service.ChatServiceImpl;
import com.pkmprojects.shoppiq.aiservice.service.ModelResolutionService;
import com.pkmprojects.shoppiq.aiservice.tools.ShoppiqTools;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

import java.time.Clock;
import java.time.Duration;

/**
 * Configures NVIDIA NIM-backed chat models and assembles the top-level
 * ChatService bean for the AI assistant.
 *
 * <p>This configuration class creates the primary synchronous and streaming
 * chat model beans using the NVIDIA NIM API with the default model
 * (Nemotron 49B). Both models are configured with identical generation
 * parameters (temperature 0.6, top-p 0.95, max tokens 4096) and a 120-second
 * timeout to ensure consistent behavior across streaming and non-streaming
 * conversation paths.</p>
 *
 * <p>The class also assembles the {@link ChatServiceImpl} bean by injecting
 * all required dependencies: chat memory providers, RAG content retrievers,
 * tool methods, persistence repositories, model resolution services, and
 * system prompt providers. The resolve threshold parameter controls how many
 * user messages must be present before auto-resolution can trigger.</p>
 *
 * @author prabhatkrmishra
 * @see ChatServiceImpl
 * @see ModelResolutionService
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "shoppiq.ai.enabled", havingValue = "true", matchIfMissing = false)
public class ChatServiceConfig {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceConfig.class);

    private final String nvidiaApiKey;
    private final int resolveThreshold;

    public ChatServiceConfig(@Value("${langchain4j.open-ai.chat-model.api-key}") String nvidiaApiKey,
                             @Value("${shoppiq.ai.resolve-threshold:3}") int resolveThreshold) {
        this.nvidiaApiKey = nvidiaApiKey;
        this.resolveThreshold = resolveThreshold;
    }

    /**
     * Creates the primary synchronous chat model bean backed by NVIDIA NIM.
     *
     * <p>Configures the OpenAI-compatible chat model with the default Nemotron 49B
     * model, a 4096-token output limit, and a 120-second timeout. Request and
     * response logging is enabled for debugging. This bean is marked as
     * {@code @Primary} so that it is injected wherever a {@link ChatModel} is
     * required without explicit qualification.</p>
     *
     * @return the primary synchronous chat model instance
     */
    @Bean
    @Primary
    public ChatModel chatModel() {
        log.debug("[AI-INIT] Creating ChatModel bean — model={}", "nvidia/llama-3.3-nemotron-super-49b-v1.5");
        return OpenAiChatModel.builder()
                .apiKey(nvidiaApiKey)
                .baseUrl("https://integrate.api.nvidia.com/v1")
                .modelName("nvidia/llama-3.3-nemotron-super-49b-v1.5")
                .maxTokens(4096)
                .temperature(0.6)
                .topP(0.95)
                .logRequests(true)
                .logResponses(true)
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    /**
     * Creates the primary streaming chat model bean backed by NVIDIA NIM.
     *
     * <p>Configures the OpenAI-compatible streaming chat model with the same
     * parameters as the synchronous model. This bean is used for real-time
     * token-by-token response delivery via Project Reactor {@link Flux}.
     * Marked as {@code @Primary} for automatic injection.</p>
     *
     * @return the primary streaming chat model instance
     */
    @Bean
    @Primary
    public StreamingChatModel streamingChatModel() {
        log.debug("[AI-INIT] Creating StreamingChatModel bean — model={}", "nvidia/llama-3.3-nemotron-super-49b-v1.5");
        return OpenAiStreamingChatModel.builder()
                .apiKey(nvidiaApiKey)
                .baseUrl("https://integrate.api.nvidia.com/v1")
                .modelName("nvidia/llama-3.3-nemotron-super-49b-v1.5")
                .maxTokens(4096)
                .temperature(0.6)
                .topP(0.95)
                .logRequests(true)
                .logResponses(true)
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    /**
     * Creates the primary {@link ChatService} bean with all required dependencies.
     *
     * <p>This factory method assembles the {@link ChatServiceImpl} by injecting
     * chat memory providers, tool methods, RAG content retrievers, persistence
     * repositories, model resolution services, and context-specific system prompt
     * providers. The resolve threshold parameter controls the minimum number of
     * user messages required before the auto-resolution heuristic can trigger.</p>
     *
     * @param chatMemoryProvider     provides per-conversation memory windows
     * @param chatMemoryConfig       manages memory lifecycle (clear on resolve)
     * @param shoppiqTools           tool methods available to the AI model
     * @param contentRetriever       RAG content retriever for product context
     * @param conversationRepository persistence for conversations
     * @param messageRepository      persistence for messages
     * @param modelResolutionService resolves model names to model instances
     * @param authenticatedPrompt    system prompt for logged-in users
     * @param guestPrompt            system prompt for guest sessions
     * @param clock                  clock for deterministic time
     * @return the assembled ChatService instance
     */
    @Bean
    public ChatService aiService(
            ChatMemoryProvider chatMemoryProvider,
            ChatMemoryConfig chatMemoryConfig,
            ShoppiqTools shoppiqTools,
            ContentRetriever contentRetriever,
            ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository,
            ModelResolutionService modelResolutionService,
            @Qualifier("authenticatedSystemPrompt") SystemPromptProvider authenticatedPrompt,
            @Qualifier("guestSystemPrompt") SystemPromptProvider guestPrompt,
            Clock clock) {

        log.info("[AI-INIT] ChatServiceImpl created — modelResolutionService={}, contentRetriever={}",
                modelResolutionService.getClass().getSimpleName(),
                contentRetriever.getClass().getSimpleName());

        return new ChatServiceImpl(
                chatMemoryProvider, chatMemoryConfig,
                shoppiqTools, contentRetriever, conversationRepository, messageRepository,
                modelResolutionService, authenticatedPrompt, guestPrompt, resolveThreshold, clock);
    }
}
