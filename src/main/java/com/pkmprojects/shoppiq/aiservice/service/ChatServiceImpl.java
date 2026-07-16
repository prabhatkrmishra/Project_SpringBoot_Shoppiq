package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.aiservice.config.ChatMemoryConfig;
import com.pkmprojects.shoppiq.aiservice.dto.ChatMessageDto;
import com.pkmprojects.shoppiq.aiservice.dto.ConversationSummary;
import com.pkmprojects.shoppiq.aiservice.entity.ChatConversation;
import com.pkmprojects.shoppiq.aiservice.entity.ChatMessage;
import com.pkmprojects.shoppiq.aiservice.enums.ChatMessageRole;
import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;
import com.pkmprojects.shoppiq.aiservice.exception.AiAssistantException;
import com.pkmprojects.shoppiq.aiservice.repository.ChatConversationRepository;
import com.pkmprojects.shoppiq.aiservice.repository.ChatMessageRepository;
import com.pkmprojects.shoppiq.aiservice.tools.ShoppiqTools;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.AiConversationNotFoundException;
import com.pkmprojects.shoppiq.repository.UserRepository;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Primary implementation of {@link ChatService} backed by LangChain4j's
 * {@link AiServices} builder pattern.
 *
 * <p>
 * This service wires together the {@link ChatModel}, {@link StreamingChatModel},
 * {@link ChatMemoryProvider}, and {@link ShoppiqTools} to create per-request
 * AI proxy instances. Each proxy call:
 * <ol>
 *   <li>Validates conversation existence and ownership</li>
 *   <li>Persists the user message to the database</li>
 *   <li>Delegates to the AI model via the LangChain4j proxy</li>
 *   <li>Persists the assistant response</li>
 *   <li>Optionally auto-resolves the conversation</li>
 * </ol>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li>Proxy instances are created per-request (not cached) to ensure fresh
 *       system prompts and tool bindings</li>
 *   <li>Guest conversations have no tool access — the proxy is built without
 *       {@code .tools()} for guest sessions</li>
 *   <li>Auto-resolution is triggered by detecting common closing phrases
 *       (e.g., "thanks", "bye", "that's all")</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private static final String NVIDIA_BASE_URL = "https://integrate.api.nvidia.com/v1";

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final ChatMemoryProvider chatMemoryProvider;
    private final ChatMemoryConfig chatMemoryConfig;
    private final ShoppiqTools shoppiqTools;
    private final ContentRetriever contentRetriever;
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final String nvidiaApiKey;

    private final Map<String, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<String, StreamingChatModel> streamingChatModelCache = new ConcurrentHashMap<>();

    /**
     * In-memory store for guest messages — not persisted to DB. Key = sessionId.
     */
    private final Map<String, List<GuestMessage>> guestMessageStore = new ConcurrentHashMap<>();

    /**
     * Lightweight record for guest messages held in memory.
     */
    public record GuestMessage(String role, String content, Instant createdAt) {
    }

    @Value("${shoppiq.ai.resolve-threshold:3}")
    private int resolveThreshold;

    /**
     * Constructs a new {@code ChatServiceImpl} with all required dependencies.
     *
     * @param chatModel              the synchronous chat model
     * @param streamingChatModel     the streaming chat model
     * @param chatMemoryProvider     provides per-conversation memory windows
     * @param chatMemoryConfig       manages chat memory lifecycle (clear on resolve)
     * @param shoppiqTools           tool methods available to the AI (product search, orders, etc.)
     * @param conversationRepository persistence for conversations
     * @param messageRepository      persistence for messages
     * @param userRepository         user lookups (unused directly but retained for future admin features)
     * @param nvidiaApiKey           NVIDIA NIM API key for dynamic model creation
     */
    public ChatServiceImpl(ChatModel chatModel,
                           StreamingChatModel streamingChatModel,
                           ChatMemoryProvider chatMemoryProvider,
                           ChatMemoryConfig chatMemoryConfig,
                           ShoppiqTools shoppiqTools,
                           ContentRetriever contentRetriever,
                           ChatConversationRepository conversationRepository,
                           ChatMessageRepository messageRepository,
                           UserRepository userRepository,
                           String nvidiaApiKey) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
        this.chatMemoryProvider = chatMemoryProvider;
        this.chatMemoryConfig = chatMemoryConfig;
        this.shoppiqTools = shoppiqTools;
        this.contentRetriever = contentRetriever;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.nvidiaApiKey = nvidiaApiKey;
    }

    /**
     * Returns a ChatModel for the given model name, using the default model when name is null/blank.
     */
    private ChatModel resolveChatModel(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return chatModel;
        }
        return chatModelCache.computeIfAbsent(modelName, name -> {
            log.debug("[AI-MODEL] Creating ChatModel for: {}", name);
            return OpenAiChatModel.builder()
                    .apiKey(nvidiaApiKey)
                    .baseUrl(NVIDIA_BASE_URL)
                    .modelName(name)
                    .maxTokens(4096)
                    .temperature(0.6)
                    .topP(0.95)
                    .logRequests(true)
                    .logResponses(true)
                    .timeout(Duration.ofSeconds(120))
                    .build();
        });
    }

    /**
     * Returns a StreamingChatModel for the given model name, using the default model when name is null/blank.
     */
    private StreamingChatModel resolveStreamingChatModel(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return streamingChatModel;
        }
        return streamingChatModelCache.computeIfAbsent(modelName, name -> {
            log.debug("[AI-MODEL] Creating StreamingChatModel for: {}", name);
            return OpenAiStreamingChatModel.builder()
                    .apiKey(nvidiaApiKey)
                    .baseUrl(NVIDIA_BASE_URL)
                    .modelName(name)
                    .maxTokens(4096)
                    .temperature(0.6)
                    .topP(0.95)
                    .logRequests(true)
                    .logResponses(true)
                    .timeout(Duration.ofSeconds(120))
                    .build();
        });
    }

    // ========================= Authenticated Chat =========================

    /**
     * {@inheritDoc}
     *
     * <p>
     * Builds a {@link ShoppiqAssistant} proxy with tool access and a user-specific
     * system prompt that includes the user's identity and conversation context.
     */
    @Override
    public String chat(String userMessage, String chatId, User user, String model) {
        ChatConversation conv = resolveConversationEntity(chatId, user);
        checkResolved(conv);

        saveMessage(conv, ChatMessageRole.USER, userMessage);
        updateTitleFromFirstMessage(conv, userMessage);

        String systemPrompt = buildSystemPrompt(conv.getChatId(), user);
        ChatModel resolvedModel = resolveChatModel(model);

        ShoppiqAssistant proxy = AiServices.builder(ShoppiqAssistant.class)
                .chatModel(resolvedModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(memoryId -> systemPrompt)
                .tools(shoppiqTools)
                .contentRetriever(contentRetriever)
                .build();

        String response;
        try {
            response = proxy.chat(userMessage, chatId);
        } catch (Exception e) {
            log.error("AI model call failed for conversation {}: {}", chatId, e.getMessage(), e);
            throw AiAssistantException.apiError("AI assistant is temporarily unavailable. Please try again.");
        }

        saveMessage(conv, ChatMessageRole.ASSISTANT, response);

        if (shouldAutoResolve(userMessage, conv)) {
            resolveConversation(conv.getChatId(), user);
        }

        return response;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Builds a {@link ShoppiqStreamingAssistant} proxy that returns tokens
     * incrementally via {@link Flux}. Tool access is included.
     */
    @Override
    public Flux<String> chatStream(String userMessage, String chatId, User user, String model) {
        ChatConversation conv = resolveConversationEntity(chatId, user);
        checkResolved(conv);

        saveMessage(conv, ChatMessageRole.USER, userMessage);
        updateTitleFromFirstMessage(conv, userMessage);

        String systemPrompt = buildSystemPrompt(conv.getChatId(), user);
        StringBuilder fullResponse = new StringBuilder();
        StreamingChatModel resolvedStreamingModel = resolveStreamingChatModel(model);

        ShoppiqStreamingAssistant proxy = AiServices.builder(ShoppiqStreamingAssistant.class)
                .streamingChatModel(resolvedStreamingModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(memoryId -> systemPrompt)
                .tools(shoppiqTools)
                .contentRetriever(contentRetriever)
                .build();

        return proxy.chat(userMessage, chatId)
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    String response = fullResponse.toString();
                    saveMessage(conv, ChatMessageRole.ASSISTANT, response);
                    log.debug("Streaming completed for conversation {}, {} chars", chatId, response.length());

                    if (shouldAutoResolve(userMessage, conv)) {
                        resolveConversation(conv.getChatId(), user);
                    }
                })
                .doOnError(error -> {
                    log.error("Streaming error for conversation {}: {}", chatId, error.getMessage());
                    saveMessage(conv, ChatMessageRole.ASSISTANT, "I'm sorry, an error occurred. Please try again.");
                });
    }

    // ========================= Conversation Management =========================

    /**
     * {@inheritDoc}
     *
     * <p>
     * Generates a unique chat ID in the format {@code CHAT-yyyy-MM-XXXX} where
     * {@code XXXX} is a random alphanumeric suffix. Uniqueness is guaranteed
     * via a retry loop against {@link ChatConversationRepository#existsByChatId(String)}.
     */
    @Override
    public ChatConversation createConversation(User user) {
        String chatId = generateChatId();
        ChatConversation conv = ChatConversation.builder()
                .user(user)
                .chatId(chatId)
                .title("New Conversation")
                .status(ConversationStatus.ACTIVE)
                .build();
        return conversationRepository.save(conv);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Each summary includes a user-message count derived from
     * {@link ChatMessageRepository#countByConversationIdAndRole(Long, ChatMessageRole)}.
     */
    @Override
    public List<ConversationSummary> getConversations(User user) {
        List<ChatConversation> conversations = conversationRepository
                .findByUserIdOrderByUpdatedAtDesc(user.getId());

        return conversations.stream()
                .map(conv -> {
                    int msgCount = (int) messageRepository
                            .countByConversationIdAndRole(conv.getId(), ChatMessageRole.USER);
                    return new ConversationSummary(
                            conv.getChatId(),
                            conv.getTitle(),
                            conv.getStatus().name(),
                            msgCount,
                            conv.getCreatedAt(),
                            conv.getUpdatedAt()
                    );
                })
                .toList();
    }

    /**
     * {@inheritDoc}
     *
     * @throws AiConversationNotFoundException if no conversation matches the given chat ID
     */
    @Override
    public List<ChatMessageDto> getMessages(String chatId, User user) {
        ChatConversation conv = resolveConversationEntity(chatId, user);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conv.getId())
                .stream()
                .map(msg -> new ChatMessageDto(
                        msg.getId(),
                        msg.getRole().name(),
                        msg.getContent(),
                        msg.getToolName(),
                        msg.getCreatedAt()
                ))
                .toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Appends a {@link ChatMessageRole#SYSTEM} message to the conversation
     * history recording the resolution event. Also clears the in-memory
     * chat history to free resources.
     *
     * <p>
     * No-op if the conversation is already resolved (idempotent).
     */
    @Override
    public void resolveConversation(String chatId, User user) {
        ChatConversation conv = resolveConversationEntity(chatId, user);

        if (conv.getStatus() == ConversationStatus.RESOLVED) {
            log.debug("Conversation {} already resolved — skipping", chatId);
            return;
        }

        conv.setStatus(ConversationStatus.RESOLVED);
        conv.setResolvedAt(Instant.now());
        conversationRepository.save(conv);

        saveMessage(conv, ChatMessageRole.SYSTEM, "Conversation resolved.");
        chatMemoryConfig.clearMemory(chatId);
    }

    // ========================= Guest Chat =========================

    /**
     * {@inheritDoc}
     *
     * <p>
     * Guest conversations are created on-the-fly if no active conversation
     * exists for the given session ID. No tool access is provided.
     */
    @Override
    public String guestChat(String userMessage, String sessionId, String model) {
        saveGuestMessage(sessionId, "USER", userMessage);

        String systemPrompt = buildGuestSystemPrompt();
        ChatModel resolvedModel = resolveChatModel(model);

        ShoppiqAssistant proxy = AiServices.builder(ShoppiqAssistant.class)
                .chatModel(resolvedModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(memoryId -> systemPrompt)
                .contentRetriever(contentRetriever)
                .build();

        String chatId = "guest-" + sessionId;
        String response = proxy.chat(userMessage, chatId);

        saveGuestMessage(sessionId, "ASSISTANT", response);

        return response;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Guest streaming variant — no tool access, uses a guest-specific
     * system prompt.
     */
    @Override
    public Flux<String> guestChatStream(String userMessage, String sessionId, String model) {
        saveGuestMessage(sessionId, "USER", userMessage);

        String systemPrompt = buildGuestSystemPrompt();
        StringBuilder fullResponse = new StringBuilder();
        StreamingChatModel resolvedStreamingModel = resolveStreamingChatModel(model);

        String chatId = "guest-" + sessionId;
        ShoppiqStreamingAssistant proxy = AiServices.builder(ShoppiqStreamingAssistant.class)
                .streamingChatModel(resolvedStreamingModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(memoryId -> systemPrompt)
                .contentRetriever(contentRetriever)
                .build();

        return proxy.chat(userMessage, chatId)
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    String response = fullResponse.toString();
                    saveGuestMessage(sessionId, "ASSISTANT", response);
                    log.debug("Guest streaming completed for session {}, {} chars", sessionId, response.length());
                })
                .doOnError(error -> {
                    log.error("Guest streaming error for session {}: {}", sessionId, error.getMessage());
                });
    }

    // ========================= Internal Helpers =========================

    /**
     * Resolves a conversation entity by chat ID and validates ownership.
     *
     * @param chatId the public conversation identifier
     * @param user   the requesting user (for ownership check)
     * @return the conversation entity
     * @throws AiConversationNotFoundException if the conversation does not exist
     * @throws AiAssistantException            if the user does not own the conversation
     */
    private ChatConversation resolveConversationEntity(String chatId, User user) {
        ChatConversation conv = conversationRepository.findByChatId(chatId)
                .orElseThrow(() -> AiConversationNotFoundException.chatId(chatId));

        if (conv.getUser() == null || !conv.getUser().getId().equals(user.getId())) {
            throw AiAssistantException.apiError("You do not have access to this conversation.");
        }
        return conv;
    }

    /**
     * Checks whether the conversation has been resolved and throws if so.
     *
     * @param conv the conversation to check
     * @throws AiAssistantException if the conversation status is {@link ConversationStatus#RESOLVED}
     */
    private void checkResolved(ChatConversation conv) {
        if (conv.getStatus() == ConversationStatus.RESOLVED) {
            throw AiAssistantException.conversationResolved();
        }
    }

    /**
     * Persists a single message to the database.
     *
     * @param conversation the parent conversation entity
     * @param role         the message role
     * @param content      the message text
     */
    private void saveMessage(ChatConversation conversation, ChatMessageRole role, String content) {
        ChatMessage msg = ChatMessage.builder()
                .conversation(conversation)
                .role(role)
                .content(content)
                .build();
        messageRepository.save(msg);
    }

    /**
     * Auto-generates a conversation title from the user's first message.
     *
     * <p>
     * If the title is still the default "New Conversation", it is replaced
     * with the first 50 characters of the message (truncated with "..." if longer).
     *
     * @param conv    the conversation to update
     * @param message the user's first message
     */
    private void updateTitleFromFirstMessage(ChatConversation conv, String message) {
        if ("New Conversation".equals(conv.getTitle())) {
            String title = message.length() > 50
                    ? message.substring(0, 50) + "..."
                    : message;
            conv.setTitle(title);
            conversationRepository.save(conv);
        }
    }

    /**
     * Determines whether the conversation should be auto-resolved based on
     * the user's message content.
     *
     * <p>
     * Auto-resolution is triggered when:
     * <ul>
     *   <li>The conversation has at least 2 user messages</li>
     *   <li>The user's message matches common closing phrases
     *       (e.g., "thanks", "bye", "that's all")</li>
     * </ul>
     *
     * @param userMessage  the user's latest message
     * @param conversation the current conversation
     * @return {@code true} if the conversation should be auto-resolved
     */
    private boolean shouldAutoResolve(String userMessage, ChatConversation conversation) {
        long userMessageCount = messageRepository
                .countByConversationIdAndRole(conversation.getId(), ChatMessageRole.USER);
        if (userMessageCount < resolveThreshold) return false;

        String normalized = userMessage.trim().toLowerCase();

        List<String> closingPhrases = List.of(
                "no", "nope", "nothing else", "that's all", "that is all",
                "thanks", "thank you", "thank", "done", "bye", "goodbye",
                "we are good", "i'm good", "i am good", "not anymore", "nah"
        );

        return closingPhrases.stream()
                .anyMatch(phrase -> normalized.equals(phrase)
                        || normalized.startsWith(phrase + " ")
                        || normalized.endsWith(" " + phrase)
                        || normalized.contains(" " + phrase + " "));
    }

    /**
     * Builds the system prompt for authenticated user conversations.
     *
     * <p>
     * The prompt includes the user's identity, chat ID, and behavioral
     * guidelines for product recommendations, order lookups, and conversation
     * closure detection.
     *
     * @param chatId the conversation's public identifier
     * @param user   the authenticated user
     * @return the system prompt text
     */
    private String buildSystemPrompt(String chatId, User user) {
        return """
                You are Shoppiq's AI shopping assistant. The user you are talking to is
                LOGGED IN as "%s" (account ID %d). They have full access to their orders,
                cart, and reviews.
                
                You have access to the Shoppiq database through function-calling tools.
                You MUST use these tools — never guess or fabricate:
                - When the user asks about their orders → call the order status tool
                - When the user asks about their cart → call the cart contents tool
                - When the user asks about their reviews → call the reviews tool
                - When the user asks for product recommendations or searches → call the
                  semantic product search tool, or rely on retrieved context
                - When the user asks about a specific product by name → call the product
                  detail tool
                
                Relevant product information may be retrieved automatically and provided
                to you as context before each message. Prefer that retrieved context when
                recommending or describing products, and always include the product link
                (/item/{slug}) and current price when you mention a product.
                
                Chat ID: %s
                
                Guidelines:
                - Be helpful, concise, and friendly
                - When recommending products, include prices and direct links (/item/{slug})
                - For order issues, provide order number and status
                - Never fabricate product information — rely on retrieved context and tools
                - If a tool returns no results, say so honestly
                - After answering the user's question, ask "Is there anything else I can help you with?"
                - If the user indicates they are done (e.g., "no", "thanks", "that's all"), respond with a closing message
                """.formatted(user.getUsername(), user.getId(), chatId);
    }

    /**
     * Builds the system prompt for guest (unauthenticated) conversations.
     *
     * <p>
     * The guest prompt is more limited — it does not include order, cart, or
     * review tools, only product catalog search via the retrieval pipeline.
     *
     * @return the guest system prompt text
     */
    private String buildGuestSystemPrompt() {
        return """
                You are Shoppiq's AI shopping assistant. The user you are talking to is a
                GUEST — they are NOT logged in and have NO account.
                
                You can ONLY help with product discovery and general shopping questions.
                Relevant product information may be retrieved automatically and provided to
                you as context before each message. Use that context to recommend and
                describe products, and always include the product link (/item/{slug})
                and current price.
                
                You do NOT have access to orders, carts, or reviews for guest users.
                If the user asks about their orders, cart, or reviews, respond with a
                clear message like:
                  "As a guest, I can't check your orders, cart, or reviews. Please
                   sign in to access those features."
                If they ask to buy or check out, say:
                  "To purchase items, you'll need to create an account and sign in.
                   Would you like me to help you find products in the meantime?"
                
                After answering the user's question, ask "Is there anything else I can help you with?"
                If the user indicates they are done, provide a friendly closing.
                """;
    }

    /**
     * Generates a unique chat ID in the format {@code CHAT-yyyy-MM-XXXX}.
     *
     * <p>
     * The prefix includes the current year and month for human readability.
     * The suffix is a 4-character random alphanumeric string. A retry loop
     * ensures uniqueness against the database.
     *
     * @return a unique chat ID string
     */
    private String generateChatId() {
        String prefix = "CHAT-" + YearMonth.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String suffix;
        do {
            suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        } while (conversationRepository.existsByChatId(prefix + "-" + suffix));
        return prefix + "-" + suffix;
    }

    // ========================= Guest History & Resolve =========================

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChatMessageDto> getGuestMessages(String sessionId) {
        List<GuestMessage> messages = guestMessageStore.getOrDefault(sessionId, List.of());
        List<GuestMessage> snapshot;
        if (messages instanceof java.util.concurrent.CopyOnWriteArrayList<GuestMessage>) {
            snapshot = messages;
        } else {
            synchronized (messages) {
                snapshot = new ArrayList<>(messages);
            }
        }
        var counter = new java.util.concurrent.atomic.AtomicLong(1L);
        return snapshot.stream()
                .map(msg -> new ChatMessageDto(
                        counter.getAndIncrement(),
                        msg.role(),
                        msg.content(),
                        null,
                        msg.createdAt()
                ))
                .toList();
    }

    @Override
    public void resolveGuestConversation(String sessionId) {
        guestMessageStore.remove(sessionId);
        chatMemoryConfig.clearMemory("guest-" + sessionId);
        log.debug("Guest conversation and memory cleared for session {}", sessionId);
    }

    // ========================= Auto-Resolve Scheduled Task =========================

    /**
     * Periodically scans for inactive conversations and resolves them.
     *
     * <p>
     * Runs every 5 minutes. Finds all ACTIVE conversations with no activity
     * for 30+ minutes, adds a SYSTEM message, and marks them as RESOLVED.
     */
    @Scheduled(fixedRate = 300_000, initialDelay = 60_000)
    public void autoResolveInactiveConversations() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(30));
        List<ChatConversation> inactive = conversationRepository
                .findByStatusAndUpdatedAtBefore(ConversationStatus.ACTIVE, cutoff);

        if (inactive.isEmpty()) {
            return;
        }

        log.info("[AUTO-RESOLVE] Found {} inactive conversations to resolve", inactive.size());

        for (ChatConversation conv : inactive) {
            try {
                conv.setStatus(ConversationStatus.RESOLVED);
                conv.setResolvedAt(Instant.now());
                conversationRepository.save(conv);

                saveMessage(conv, ChatMessageRole.SYSTEM, "Conversation auto-resolved due to inactivity.");
                chatMemoryConfig.clearMemory(conv.getChatId());
                log.debug("[AUTO-RESOLVE] Resolved conversation {} and cleared memory", conv.getChatId());
            } catch (Exception e) {
                log.error("[AUTO-RESOLVE] Failed to resolve conversation {}: {}", conv.getChatId(), e.getMessage());
            }
        }
    }

    // ========================= Guest In-Memory Helpers =========================

    private void saveGuestMessage(String sessionId, String role, String content) {
        guestMessageStore.computeIfAbsent(sessionId, k -> java.util.Collections.synchronizedList(new ArrayList<>()))
                .add(new GuestMessage(role, content, Instant.now()));
    }
}
