package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.aiservice.exception.AiModelNotSupportedException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized service for resolving AI model names to LangChain4j model instances.
 *
 * <p>This service maintains a registry of allowed NVIDIA NIM model IDs with
 * lazy instantiation and caching. When the frontend specifies a model in a
 * chat request, this service resolves it to the corresponding synchronous or
 * streaming {@link ChatModel} instance. Unrecognized model IDs are rejected
 * with an {@link AiModelNotSupportedException}.</p>
 *
 * <p>Model instances are cached in thread-safe maps to avoid recreating
 * them for repeated requests. The default model (Nemotron Nano Omni 30B) is provided
 * as a pre-configured bean and is returned directly without caching overhead
 * when no model selection is specified.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Service
public class ModelResolutionService {

    /**
     * The default model ID used when no model is specified or an invalid model is requested.
     */
    public static final String DEFAULT_MODEL_ID = "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning";
    private static final Logger log = LoggerFactory.getLogger(ModelResolutionService.class);
    private static final String NVIDIA_BASE_URL = "https://integrate.api.nvidia.com/v1";
    private static final Duration MODEL_TIMEOUT = Duration.ofSeconds(300);
    /**
     * Maps assistant numbers sent by the frontend to actual model IDs.
     * Frontend sends "1", "2", or "3" — this map resolves them to the real model.
     */
    private static final Map<String, String> ASSISTANT_MODEL_MAP = Map.of(
            "1", "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning",
            "2", "nvidia/nemotron-3-nano-30b-a3b",
            "3", "nvidia/llama-3.3-nemotron-super-49b-v1.5"
    );
    /**
     * Registry of allowed model IDs mapped to their display names.
     * Only models in this registry can be used by the frontend.
     */
    private static final Map<String, String> MODEL_REGISTRY = Map.of(
            "nvidia/llama-3.3-nemotron-super-49b-v1.5", "Nemotron 49B",
            "nvidia/nemotron-3-nano-30b-a3b", "Nemotron Nano 30B",
            "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning", "Nemotron Nano Omni 30B"
    );

    /**
     * Per-model thinking control:
     * - Nemotron 49B: disables thinking via "/no_think" in the system prompt.
     * - Nemotron Nano 30B / Omni 30B: disables thinking via "chat_template_kwargs"
     * in the request body (these models ignore the system-prompt directive).
     * <p>
     * Reference: <a href="https://docs.nvidia.com/nim/large-language-models/1.8.0/reasoning-model.html">Nvidia Docs</a>
     */
    public static final Set<String> CHAT_TEMPLATE_THINKING_MODELS = Set.of(
            "nvidia/nemotron-3-nano-30b-a3b",
            "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning"
    );
    public static final Map<String, Object> CHAT_TEMPLATE_DISABLE_THINKING = Map.of(
            "chat_template_kwargs", Map.of("enable_thinking", false)
    );
    private final ChatModel defaultChatModel;
    private final StreamingChatModel defaultStreamingChatModel;
    private final String nvidiaApiKey;
    private final Map<String, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<String, StreamingChatModel> streamingModelCache = new ConcurrentHashMap<>();

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
     * <p>If the model name is null, blank, or not in the registry, the default
     * model is returned. Otherwise, a new model instance is created (or retrieved
     * from cache) for the requested model name. All non-default models are
     * configured with the same generation parameters as the default model.</p>
     *
     * <p>The Nemotron Nano 30B and Omni 30B models are additionally configured
     * with {@code chat_template_kwargs: {"enable_thinking": false}} in the
     * request body, which is the supported mechanism for disabling reasoning
     * content on these models. The 49B model uses {@code /no_think}
     * in the system prompt instead.</p>
     *
     * @param modelName the model identifier from the frontend (e.g., "1" for Omni, "3" for 49B)
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
            var builder = OpenAiChatModel.builder()
                    .apiKey(nvidiaApiKey)
                    .baseUrl(NVIDIA_BASE_URL)
                    .modelName(name)
                    .maxTokens(4096)
                    .temperature(0.6)
                    .topP(0.95)
                    .logRequests(true)
                    .logResponses(true)
                    .timeout(MODEL_TIMEOUT);
            if (CHAT_TEMPLATE_THINKING_MODELS.contains(name)) {
                builder.customParameters(CHAT_TEMPLATE_DISABLE_THINKING);
            }
            return builder.build();
        });
    }

    /**
     * Resolves a model name to a streaming {@link StreamingChatModel}.
     *
     * <p>If the model name is null, blank, or not in the registry, the default
     * streaming model is returned. Otherwise, a new streaming model instance is
     * created (or retrieved from cache) for the requested model name. All
     * non-default models are configured with the same generation parameters.</p>
     *
     * <p>The Nemotron Nano 30B and Omni 30B models are additionally configured
     * with {@code chat_template_kwargs: {"enable_thinking": false}} in the
     * request body, which is the supported mechanism for disabling reasoning
     * content on these models. The 49B model uses {@code /no_think}
     * in the system prompt instead.</p>
     *
     * @param modelName the model identifier from the frontend (e.g., "1" for Omni, "3" for 49B)
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
            var builder = OpenAiStreamingChatModel.builder()
                    .apiKey(nvidiaApiKey)
                    .baseUrl(NVIDIA_BASE_URL)
                    .modelName(name)
                    .maxTokens(4096)
                    .temperature(0.6)
                    .topP(0.95)
                    .logRequests(true)
                    .logResponses(true)
                    .timeout(MODEL_TIMEOUT);
            if (CHAT_TEMPLATE_THINKING_MODELS.contains(name)) {
                builder.customParameters(CHAT_TEMPLATE_DISABLE_THINKING);
            }
            return builder.build();
        });
    }

    /**
     * Returns the set of all allowed model IDs.
     *
     * <p>Used by the frontend to populate the model selection dropdown.
     * The returned set is unmodifiable and reflects the current model
     * registry configuration.</p>
     *
     * @return unmodifiable set of model ID strings
     */
    public Set<String> getAllowedModelIds() {
        return MODEL_REGISTRY.keySet();
    }

    /**
     * Returns the display name for a given model ID.
     *
     * <p>Maps the technical model identifier to a human-readable display
     * name suitable for the frontend model selector. Returns the model ID
     * itself if no display name mapping exists.</p>
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
     * <p>Used for input validation before attempting model resolution.
     * Returns false for null inputs.</p>
     *
     * @param modelId the model identifier to check
     * @return true if the model is registered and allowed
     */
    public boolean isAllowedModel(String modelId) {
        return modelId != null && MODEL_REGISTRY.containsKey(modelId);
    }

    /**
     * Sanitizes and validates the model name.
     *
     * <p>Resolves assistant numbers ("1", "2", "3") to their corresponding
     * model IDs via {@link #ASSISTANT_MODEL_MAP}. Returns the default model
     * ID if the input is null or blank. Throws {@link AiModelNotSupportedException}
     * if the model is explicitly provided but not found in the allowed registry.
     * Trims whitespace from the input before validation.</p>
     *
     * @param modelName the raw model name or assistant number from the request
     * @return a valid, allowed model ID
     * @throws AiModelNotSupportedException if the model is not null/blank but not in the registry
     */
    private String sanitizeModelName(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return DEFAULT_MODEL_ID;
        }

        String trimmed = modelName.trim();

        /* Resolve assistant numbers ("1", "2", "3") to actual model IDs. */
        String resolved = ASSISTANT_MODEL_MAP.getOrDefault(trimmed, trimmed);

        if (MODEL_REGISTRY.containsKey(resolved)) {
            return resolved;
        }

        log.warn("[AI-MODEL] Unknown model requested: '{}'", trimmed);
        throw AiModelNotSupportedException.forModel(trimmed);
    }
}
