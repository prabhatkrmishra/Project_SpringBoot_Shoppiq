package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.aiservice.exception.AiModelNotSupportedException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized service for resolving AI model names to LangChain4j
 * {@link ChatModel} and {@link StreamingChatModel} instances.
 *
 * <p>
 * Maintains a registry of allowed model IDs and their display names.
 * Models are lazily instantiated and cached for the application lifetime.
 * Requests for unknown or disallowed model names fall back to the default model.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
public class ModelResolutionService {

    private static final Logger log = LoggerFactory.getLogger(ModelResolutionService.class);

    private static final String NVIDIA_BASE_URL = "https://integrate.api.nvidia.com/v1";
    private static final Duration MODEL_TIMEOUT = Duration.ofSeconds(300);

    private final ChatModel defaultChatModel;
    private final StreamingChatModel defaultStreamingChatModel;
    private final String nvidiaApiKey;

    private final Map<String, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<String, StreamingChatModel> streamingModelCache = new ConcurrentHashMap<>();

/**
     * Registry of allowed model IDs mapped to their display names.
     * Only models in this registry can be used by the frontend.
     */
    private static final Map<String, String> MODEL_REGISTRY = Map.of(
            "nvidia/llama-3.3-nemotron-super-49b-v1.5", "Nemotron 49B",
            "nvidia/nemotron-3-nano-30b-a3b", "Nemotron Nano 30B"
    );

    /**
     * The default model ID used when no model is specified or an invalid model is requested.
     */
    public static final String DEFAULT_MODEL_ID = "nvidia/llama-3.3-nemotron-super-49b-v1.5";

    public ModelResolutionService(
            ChatModel defaultChatModel,
            StreamingChatModel defaultStreamingChatModel,
            @Value("${langchain4j.open-ai.chat-model.api-key}") String nvidiaApiKey) {
        this.defaultChatModel = defaultChatModel;
        this.defaultStreamingChatModel = defaultStreamingChatModel;
        this.nvidiaApiKey = nvidiaApiKey;
    }

    @PostConstruct
    void init() {
        log.info("[AI-MODEL] ModelResolutionService initialized — registered models: {}", MODEL_REGISTRY.keySet());
    }

    /**
     * Resolves a model name to a synchronous {@link ChatModel}.
     *
     * <p>
     * If the model name is null, blank, or not in the registry, the default
     * model is returned. Otherwise, a new model instance is created (or cached)
     * for the requested model name.
     *
     * @param modelName the model identifier from the frontend (e.g., "nvidia/llama-3.3-nemotron-super-49b-v1.5")
     * @return the resolved ChatModel instance
     */
    public ChatModel resolveChatModel(String modelName) {
        String resolvedName = sanitizeModelName(modelName);

        if (resolvedName.equals(DEFAULT_MODEL_ID)) {
            log.debug("[AI-MODEL] Using default ChatModel: {}", DEFAULT_MODEL_ID);
            return defaultChatModel;
        }

        return chatModelCache.computeIfAbsent(resolvedName, name -> {
            log.info("[AI-MODEL] Creating new ChatModel for: {}", name);
            return OpenAiChatModel.builder()
                    .apiKey(nvidiaApiKey)
                    .baseUrl(NVIDIA_BASE_URL)
                    .modelName(name)
                    .maxTokens(4096)
                    .temperature(0.6)
                    .topP(0.95)
                    .logRequests(true)
                    .logResponses(true)
                    .timeout(MODEL_TIMEOUT)
                    .build();
        });
    }

    /**
     * Resolves a model name to a streaming {@link StreamingChatModel}.
     *
     * <p>
     * If the model name is null, blank, or not in the registry, the default
     * streaming model is returned. Otherwise, a new streaming model instance
     * is created (or cached) for the requested model name.
     *
     * @param modelName the model identifier from the frontend
     * @return the resolved StreamingChatModel instance
     */
    public StreamingChatModel resolveStreamingChatModel(String modelName) {
        String resolvedName = sanitizeModelName(modelName);

        if (resolvedName.equals(DEFAULT_MODEL_ID)) {
            log.debug("[AI-MODEL] Using default StreamingChatModel: {}", DEFAULT_MODEL_ID);
            return defaultStreamingChatModel;
        }

        return streamingModelCache.computeIfAbsent(resolvedName, name -> {
            log.info("[AI-MODEL] Creating new StreamingChatModel for: {}", name);
            return OpenAiStreamingChatModel.builder()
                    .apiKey(nvidiaApiKey)
                    .baseUrl(NVIDIA_BASE_URL)
                    .modelName(name)
                    .maxTokens(4096)
                    .temperature(0.6)
                    .topP(0.95)
                    .logRequests(true)
                    .logResponses(true)
                    .timeout(MODEL_TIMEOUT)
                    .build();
        });
    }

    /**
     * Returns the set of all allowed model IDs.
     *
     * @return unmodifiable set of model ID strings
     */
    public Set<String> getAllowedModelIds() {
        return MODEL_REGISTRY.keySet();
    }

    /**
     * Returns the display name for a given model ID.
     *
     * @param modelId the model identifier
     * @return the display name, or the model ID itself if not found
     */
    public String getModelDisplayName(String modelId) {
        return MODEL_REGISTRY.getOrDefault(modelId, modelId);
    }

    /**
     * Checks whether a given model ID is in the allowed registry.
     *
     * @param modelId the model identifier to check
     * @return true if the model is registered and allowed
     */
    public boolean isAllowedModel(String modelId) {
        return modelId != null && MODEL_REGISTRY.containsKey(modelId);
    }

    /**
     * Sanitizes and validates the model name. Returns the default model ID
     * if the input is null or blank. Throws {@link AiModelNotSupportedException}
     * if the model is explicitly provided but not in the allowed registry.
     *
     * @param modelName the raw model name from the request
     * @return a valid, allowed model ID
     * @throws AiModelNotSupportedException if the model is not null/blank but not in the registry
     */
    private String sanitizeModelName(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return DEFAULT_MODEL_ID;
        }

        String trimmed = modelName.trim();

        if (MODEL_REGISTRY.containsKey(trimmed)) {
            return trimmed;
        }

        log.warn("[AI-MODEL] Unknown model requested: '{}'", trimmed);
        throw AiModelNotSupportedException.forModel(trimmed);
    }
}
