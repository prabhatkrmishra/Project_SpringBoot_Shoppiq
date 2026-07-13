package com.pkmprojects.shoppiq.aiservice.config;

import com.pkmprojects.shoppiq.aiservice.repository.ChatConversationRepository;
import com.pkmprojects.shoppiq.aiservice.repository.ChatMessageRepository;
import com.pkmprojects.shoppiq.aiservice.service.ChatService;
import com.pkmprojects.shoppiq.aiservice.service.ChatServiceImpl;
import com.pkmprojects.shoppiq.aiservice.tools.ShoppiqTools;
import com.pkmprojects.shoppiq.repository.UserRepository;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Spring configuration that creates the NVIDIA NIM-backed {@link ChatModel}
 * and {@link StreamingChatModel} beans, and wires them into {@link ChatServiceImpl}.
 *
 * <p>
 * Only active when the {@code shoppiq.ai.enabled=true} property is set. The default model
 * is {@code nvidia/llama-3.3-nemotron-super-49b-v1.5}. Additional models are
 * created dynamically by {@link ChatServiceImpl} on demand and cached per model name.
 *
 * <h2>Beans Provided</h2>
 * <ul>
 *   <li>{@link ChatModel} — synchronous chat model</li>
 *   <li>{@link StreamingChatModel} — streaming (token-by-token) chat model</li>
 *   <li>{@link ChatService} — the main AI service implementation</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @see ChatServiceImpl
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "shoppiq.ai.enabled", havingValue = "true", matchIfMissing = false)
public class ChatServiceConfig {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceConfig.class);

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String nvidiaApiKey;

    @Bean
    public ChatModel chatModel() {
        log.debug("[AI-INIT] Creating ChatModel bean — model={}", "nvidia/llama-3.3-nemotron-super-49b-v1.5");
        return OpenAiChatModel.builder()
                .apiKey(nvidiaApiKey)
                .baseUrl("https://integrate.api.nvidia.com/v1")
                .modelName("nvidia/llama-3.3-nemotron-super-49b-v1.5")
                .maxTokens(4096)
                .logRequests(true)
                .logResponses(true)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public StreamingChatModel streamingChatModel() {
        log.debug("[AI-INIT] Creating StreamingChatModel bean — model={}", "nvidia/llama-3.3-nemotron-super-49b-v1.5");
        return OpenAiStreamingChatModel.builder()
                .apiKey(nvidiaApiKey)
                .baseUrl("https://integrate.api.nvidia.com/v1")
                .modelName("nvidia/llama-3.3-nemotron-super-49b-v1.5")
                .maxTokens(4096)
                .logRequests(true)
                .logResponses(true)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public ChatService aiService(
            ChatModel chatModel,
            StreamingChatModel streamingChatModel,
            ChatMemoryProvider chatMemoryProvider,
            ChatMemoryConfig chatMemoryConfig,
            ShoppiqTools shoppiqTools,
            ContentRetriever contentRetriever,
            ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository,
            UserRepository userRepository) {

        log.info("[AI-INIT] ChatServiceImpl created — chatModel={}, streamingChatModel={}, contentRetriever={}",
                chatModel.getClass().getSimpleName(),
                streamingChatModel.getClass().getSimpleName(),
                contentRetriever.getClass().getSimpleName());

        return new ChatServiceImpl(
                chatModel, streamingChatModel, chatMemoryProvider, chatMemoryConfig,
                shoppiqTools, contentRetriever, conversationRepository, messageRepository, userRepository,
                nvidiaApiKey);
    }
}