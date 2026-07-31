package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.aiservice.config.ChatMemoryConfig;
import com.pkmprojects.shoppiq.aiservice.dto.ChatMessageDto;
import com.pkmprojects.shoppiq.aiservice.dto.ConversationSummary;
import com.pkmprojects.shoppiq.aiservice.entity.ChatConversation;
import com.pkmprojects.shoppiq.aiservice.entity.ChatMessage;
import com.pkmprojects.shoppiq.aiservice.enums.ChatMessageRole;
import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;
import com.pkmprojects.shoppiq.aiservice.exception.AiAccessDeniedException;
import com.pkmprojects.shoppiq.aiservice.exception.AiAssistantException;
import com.pkmprojects.shoppiq.aiservice.instructions.SystemPromptProvider;
import com.pkmprojects.shoppiq.aiservice.repository.ChatConversationRepository;
import com.pkmprojects.shoppiq.aiservice.repository.ChatMessageRepository;
import com.pkmprojects.shoppiq.aiservice.tools.ShoppiqTools;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.exception.general.aiservice.AiConversationNotFoundException;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;

import java.time.Clock;
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
 * Primary implementation of {@link ChatService} backed by LangChain4j's AiServices.
 *
 * <p>This class orchestrates the complete AI chat workflow by wiring together
 * LangChain4j chat models, memory providers, tool methods, RAG content
 * retrievers, and system prompt providers into per-request AI proxy instances.
 * It handles both authenticated conversations (with full tool access and
 * database persistence) and guest conversations (with RAG-only retrieval
 * and in-memory storage).</p>
 *
 * <p>The implementation includes an auto-resolution heuristic that detects
 * when a user indicates they are done (e.g., "thanks", "bye", "done") and
 * automatically resolves the conversation. This heuristic requires a minimum
 * number of user messages and verifies that the assistant's message immediately
 * preceding the user's closing reply was a closing prompt to avoid false positives.</p>
 *
 * <p>A scheduled task runs every 5 minutes to resolve conversations that have
 * been inactive for 30 or more minutes, preventing resource leakage from
 * abandoned sessions.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatMemoryProvider chatMemoryProvider;
    private final ChatMemoryConfig chatMemoryConfig;
    private final ShoppiqTools shoppiqTools;
    private final ContentRetriever contentRetriever;
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ModelResolutionService modelResolutionService;
    private final SystemPromptProvider authenticatedPrompt;
    private final SystemPromptProvider guestPrompt;

    /**
     * In-memory store for guest messages — not persisted to DB. Key = sessionId.
     */
    private final Map<String, List<GuestMessage>> guestMessageStore = new ConcurrentHashMap<>();
    private final int resolveThreshold;
    private final Clock clock;

    /**
     * Constructs a new {@code ChatServiceImpl} with all required dependencies.
     *
     * <p>This constructor receives the full set of dependencies needed to
     * orchestrate AI chat conversations: memory providers for context windows,
     * tool methods for data access, RAG retrievers for product context,
     * persistence repositories for conversation and message storage, model
     * resolution for multimodel support, and context-specific system prompt
     * providers for authenticated and guest users.</p>
     *
     * @param chatMemoryProvider     provides per-conversation memory windows
     * @param chatMemoryConfig       manages chat memory lifecycle (clear on resolve)
     * @param shoppiqTools           tool methods available to the AI (product search, orders, etc.)
     * @param contentRetriever       RAG content retriever for product context
     * @param conversationRepository persistence for conversations
     * @param messageRepository      persistence for messages
     * @param modelResolutionService central service for resolving model names to model instances
     * @param authenticatedPrompt    system prompt for logged-in users
     * @param guestPrompt            system prompt for guest sessions
     * @param resolveThreshold       minimum user messages before auto-resolution can trigger
     * @param clock                  clock for deterministic time
     */
    public ChatServiceImpl(ChatMemoryProvider chatMemoryProvider,
                           ChatMemoryConfig chatMemoryConfig,
                           ShoppiqTools shoppiqTools,
                           ContentRetriever contentRetriever,
                           ChatConversationRepository conversationRepository,
                           ChatMessageRepository messageRepository,
                           ModelResolutionService modelResolutionService,
                           @Qualifier("authenticatedSystemPrompt") SystemPromptProvider authenticatedPrompt,
                           @Qualifier("guestSystemPrompt") SystemPromptProvider guestPrompt,
                           int resolveThreshold,
                           Clock clock) {
        this.chatMemoryProvider = chatMemoryProvider;
        this.chatMemoryConfig = chatMemoryConfig;
        this.shoppiqTools = shoppiqTools;
        this.contentRetriever = contentRetriever;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.modelResolutionService = modelResolutionService;
        this.authenticatedPrompt = authenticatedPrompt;
        this.guestPrompt = guestPrompt;
        this.resolveThreshold = resolveThreshold;
        this.clock = clock;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Builds a {@link ShoppiqAssistant} proxy with tool access and a user-specific
     * system prompt that includes the user's identity and conversation context.
     * The proxy is configured with the resolved chat model, memory provider,
     * and content retriever for RAG-augmented responses.</p>
     */
    @Override
    public String chat(String userMessage, String chatId, User user, String model) {
        ChatConversation conv = resolveConversationEntity(chatId, user);
        checkResolved(conv);

        ChatMessage userMessageEntity = saveMessage(conv, ChatMessageRole.USER, userMessage);
        updateTitleFromFirstMessage(conv, userMessage);

        String systemPrompt = authenticatedPrompt.buildPrompt(conv.getChatId(), user);
        ChatModel resolvedModel = modelResolutionService.resolveChatModel(model);

        ShoppiqAssistant proxy = AiServices.builder(ShoppiqAssistant.class)
                .chatModel(resolvedModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(memoryId -> prependNoThink(systemPrompt, model))
                .tools(shoppiqTools)
                .contentRetriever(contentRetriever)
                .build();

        String response;
        try {
            response = proxy.chat(userMessage, chatId);
        } catch (Exception e) {
            log.error("AI model call failed for conversation {}: {}", chatId, e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("timeout")) {
                throw AiAssistantException.timeout("AI assistant timed out. Please try again.");
            }
            throw AiAssistantException.apiError("AI assistant is temporarily unavailable. Please try again.");
        }

        response = postProcessResponse(response);
        saveMessage(conv, ChatMessageRole.ASSISTANT, response);

        if (shouldAutoResolve(userMessage, conv, userMessageEntity.getId())) {
            resolveConversation(conv.getChatId(), user);
        }

        return response;
    }

    // ========================= Authenticated Chat =========================

    /**
     * {@inheritDoc}
     *
     * <p>Builds a {@link ShoppiqStreamingAssistant} proxy that returns tokens
     * incrementally via {@link Flux}. Tool access is included, and the full
     * response is assembled in memory and persisted after stream completion.</p>
     */
    @Override
    public Flux<String> chatStream(String userMessage, String chatId, User user, String model) {
        ChatConversation conv = resolveConversationEntity(chatId, user);
        checkResolved(conv);

        ChatMessage userMessageEntity = saveMessage(conv, ChatMessageRole.USER, userMessage);
        updateTitleFromFirstMessage(conv, userMessage);

        String systemPrompt = authenticatedPrompt.buildPrompt(conv.getChatId(), user);
        StringBuilder fullResponse = new StringBuilder();
        StreamingChatModel resolvedStreamingModel = modelResolutionService.resolveStreamingChatModel(model);

        ShoppiqStreamingAssistant proxy = AiServices.builder(ShoppiqStreamingAssistant.class)
                .streamingChatModel(resolvedStreamingModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(memoryId -> prependNoThink(systemPrompt, model))
                .tools(shoppiqTools)
                .contentRetriever(contentRetriever)
                .build();

        return proxy.chat(userMessage, chatId)
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    String response = postProcessResponse(fullResponse.toString());
                    saveMessage(conv, ChatMessageRole.ASSISTANT, response);
                    log.debug("Streaming completed for conversation {}, {} chars", chatId, response.length());

                    if (shouldAutoResolve(userMessage, conv, userMessageEntity.getId())) {
                        resolveConversation(conv.getChatId(), user);
                    }
                })
                .doOnError(error -> {
                    log.error("Streaming error for conversation {}: {}", chatId, error.getMessage());
                    saveMessage(conv, ChatMessageRole.ASSISTANT, "I'm sorry, an error occurred. Please try again.");
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConversationStatus getConversationStatus(String chatId, User user) {
        ChatConversation conv = resolveConversationEntity(chatId, user);
        return conv.getStatus();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a unique chat ID in the format {@code CHAT-yyyy-MM-XXXX} where
     * {@code XXXX} is a random alphanumeric suffix. Uniqueness is guaranteed
     * via a retry loop against {@link ChatConversationRepository#existsByChatId(String)}.</p>
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

    // ========================= Conversation Management =========================

    /**
     * {@inheritDoc}
     *
     * <p>Each summary includes a user-message count derived from
     * {@link ChatMessageRepository#countByConversationIdAndRole(Long, ChatMessageRole)}.
     * The count helps the frontend display conversation length in the sidebar.</p>
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
     * @throws AiAccessDeniedException         if the user does not own the conversation
     */
    @Override
    public List<ChatMessageDto> getMessages(String chatId, User user) {
        ChatConversation conv = resolveConversationEntity(chatId, user);
        return messageRepository.findByConversationIdOrderByIdAsc(conv.getId())
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
     * <p>Appends a {@link ChatMessageRole#SYSTEM} message to the conversation
     * history recording the resolution event. Also clears the in-memory
     * chat window to free resources.</p>
     *
     * <p>This operation is idempotent; resolving an already-resolved
     * conversation is a no-op.</p>
     */
    @Override
    public void resolveConversation(String chatId, User user) {
        ChatConversation conv = resolveConversationEntity(chatId, user);

        if (conv.getStatus() == ConversationStatus.RESOLVED) {
            log.debug("Conversation {} already resolved — skipping", chatId);
            return;
        }

        conv.setStatus(ConversationStatus.RESOLVED);
        conv.setResolvedAt(Instant.now(clock));
        conversationRepository.save(conv);

        saveMessage(conv, ChatMessageRole.SYSTEM, "Conversation resolved.");
        chatMemoryConfig.clearMemory(chatId);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Guest conversations are created on-the-fly using a
     * {@code guest-<sessionId>} chat ID. No tool access is provided;
     * the AI model relies solely on RAG content retrieval for product
     * information.</p>
     */
    @Override
    public String guestChat(String userMessage, String sessionId, String model) {
        saveGuestMessage(sessionId, "USER", userMessage);

        String systemPrompt = guestPrompt.buildPrompt(null, null);
        ChatModel resolvedModel = modelResolutionService.resolveChatModel(model);

        ShoppiqAssistant proxy = AiServices.builder(ShoppiqAssistant.class)
                .chatModel(resolvedModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(memoryId -> prependNoThink(systemPrompt, model))
                .contentRetriever(contentRetriever)
                .build();

        String chatId = "guest-" + sessionId;
        String response;
        try {
            response = proxy.chat(userMessage, chatId);
        } catch (Exception e) {
            log.error("AI model call failed for guest session {}: {}", sessionId, e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("timeout")) {
                throw AiAssistantException.timeout("AI assistant timed out. Please try again.");
            }
            throw AiAssistantException.apiError("AI assistant is temporarily unavailable. Please try again.");
        }

        response = postProcessResponse(response);
        saveGuestMessage(sessionId, "ASSISTANT", response);

        return response;
    }

    // ========================= Guest Chat =========================

    /**
     * {@inheritDoc}
     *
     * <p>Guest streaming variant that uses a guest-specific system prompt
     * and no tool access. Tokens are returned incrementally via {@link Flux}
     * for real-time UI rendering.</p>
     */
    @Override
    public Flux<String> guestChatStream(String userMessage, String sessionId, String model) {
        saveGuestMessage(sessionId, "USER", userMessage);

        String systemPrompt = guestPrompt.buildPrompt(null, null);
        StringBuilder fullResponse = new StringBuilder();
        StreamingChatModel resolvedStreamingModel = modelResolutionService.resolveStreamingChatModel(model);

        String chatId = "guest-" + sessionId;
        ShoppiqStreamingAssistant proxy = AiServices.builder(ShoppiqStreamingAssistant.class)
                .streamingChatModel(resolvedStreamingModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(memoryId -> prependNoThink(systemPrompt, model))
                .contentRetriever(contentRetriever)
                .build();

        return proxy.chat(userMessage, chatId)
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    String response = postProcessResponse(fullResponse.toString());
                    saveGuestMessage(sessionId, "ASSISTANT", response);
                    log.debug("Guest streaming completed for session {}, {} chars", sessionId, response.length());
                })
                .doOnError(error -> {
                    log.error("Guest streaming error for session {}: {}", sessionId, error.getMessage());
                    saveGuestMessage(sessionId, "ASSISTANT", "I'm sorry, an error occurred. Please try again.");
                });
    }

    /**
     * Resolves a conversation entity by chat ID and validates ownership.
     *
     * <p>Looks up the conversation by its public chat ID and verifies that
     * the requesting user is the conversation owner. Throws if the conversation
     * does not exist or if the user does not have access.</p>
     *
     * @param chatId the public conversation identifier
     * @param user   the requesting user (for ownership check)
     * @return the conversation entity
     * @throws AiConversationNotFoundException if the conversation does not exist
     * @throws AiAccessDeniedException         if the user does not own the conversation
     */
    private ChatConversation resolveConversationEntity(String chatId, User user) {
        ChatConversation conv = conversationRepository.findByChatId(chatId)
                .orElseThrow(() -> AiConversationNotFoundException.chatId(chatId));

        if (conv.getUser() == null || !conv.getUser().getId().equals(user.getId())) {
            throw AiAccessDeniedException.forConversation(chatId);
        }
        return conv;
    }

    // ========================= Internal Helpers =========================

    /**
     * Checks whether the conversation has been resolved and throws if so.
     *
     * <p>This guard prevents further message processing on conversations that
     * have already been closed. Throws an {@link AiAssistantException} with
     * a 410 Gone status.</p>
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
     * Post-processes the AI model's response.
     *
     * <p>Trims whitespace and returns a user-friendly fallback for null or
     * blank responses. Thinking control is handled at the API level:
     * <ul>
     *   <li>49B: {@code /no_think} prepended to the system prompt externally</li>
     *   <li>Nano 30B / Omni 30B: {@code chat_template_kwargs} in the request body (via {@link ModelResolutionService})</li>
     * </ul></p>
     *
     * @param response the raw response from the AI model
     * @return the trimmed response, or a fallback message if the response is unusable
     */
    private String postProcessResponse(String response) {
        if (response == null || response.isBlank()) {
            log.warn("[AI-RESPONSE] Empty response from model — returning fallback");
            return "I'm sorry, I wasn't able to generate a response. Could you try again?";
        }
        return response.trim();
    }

    /**
     * Prepends {@code /no_think} to the system prompt for models that support it.
     *
     * <p>The Nemotron 49B model disables thinking via {@code /no_think} in the
     * system prompt. The Nano 30B and Omni 30B models use {@code chat_template_kwargs}
     * in the request body instead (configured in {@link ModelResolutionService}).</p>
     *
     * @param systemPrompt the base system prompt
     * @param model        the resolved model identifier
     * @return the system prompt, with {@code /no_think} prepended if the model supports it
     */
    private String prependNoThink(String systemPrompt, String model) {
        if (!ModelResolutionService.CHAT_TEMPLATE_THINKING_MODELS.contains(model)) {
            return "/no_think\n" + systemPrompt;
        }
        return systemPrompt;
    }

    /**
     * Persists a single message to the database.
     *
     * <p>Creates a new {@link ChatMessage} entity with the specified role and
     * content, linked to the parent conversation, and saves it via the message
     * repository. This method is used for both user and assistant messages.</p>
     *
     * @param conversation the parent conversation entity
     * @param role         the message role (USER, ASSISTANT, or SYSTEM)
     * @param content      the message text
     * @return the persisted message (with its generated id and timestamps)
     */
    private ChatMessage saveMessage(ChatConversation conversation, ChatMessageRole role, String content) {
        ChatMessage msg = ChatMessage.builder()
                .conversation(conversation)
                .role(role)
                .content(content)
                .build();
        return messageRepository.save(msg);
    }

    /**
     * Auto-generates a conversation title from the user's first message.
     *
     * <p>If the title is still the default "New Conversation", it is replaced
     * with the first 50 characters of the message (truncated with "..." if
     * longer). This provides a human-readable identifier for the conversation
     * in the sidebar list without requiring the user to manually name it.</p>
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
     * <p>Auto-resolution is triggered ONLY when ALL the following conditions hold:</p>
     *
     * <ul>
     *   <li>The conversation has at least {@code resolveThreshold} user messages</li>
     *   <li>The ASSISTANT message immediately preceding the user's message was
     *       itself a closing prompt (e.g., ended with "Is there anything else I
     *       can help you with?")</li>
     *   <li>The user's entire message (after trimming, lowercasing, and stripping
     *       trailing punctuation) exactly matches one of the known closing phrases</li>
     * </ul>
     *
     * <p>The closing-prompt check prevents false auto-resolves when a short
     * reply like "no" or "done" is answering some other assistant question
     * (e.g., "Do you want me to filter by size too?" - "no") rather than
     * confirming the user is finished.</p>
     *
     * <p>The check targets the assistant message persisted <em>before</em> the
     * current user message (identified by {@code userMessageId}), not the most
     * recent one. The current turn's assistant reply — typically a friendly
     * closing like "You're welcome!" — is persisted before this method runs and
     * would otherwise mask the closing prompt that preceded the user's reply.</p>
     *
     * @param userMessage   the user's latest message
     * @param conversation  the current conversation
     * @param userMessageId the id of the persisted user message being evaluated
     * @return {@code true} if the conversation should be auto-resolved
     */
    private boolean shouldAutoResolve(String userMessage, ChatConversation conversation, long userMessageId) {
        long userMessageCount = messageRepository
                .countByConversationIdAndRole(conversation.getId(), ChatMessageRole.USER);
        if (userMessageCount < resolveThreshold) return false;

        if (!lastAssistantMessageWasClosingPrompt(conversation, userMessageId)) {
            return false;
        }

        String normalized = userMessage.trim().toLowerCase()
                .replaceAll("[!.?]+$", "")
                .trim();

        List<String> closingPhrases = List.of(
                "no", "nope", "nothing else", "that's all", "that is all",
                "thanks", "thank you", "thanks a lot", "thank you so much", "tata",
                "done", "bye", "goodbye", "we are good", "we're good",
                "i'm good", "i am good", "not anymore", "nah", "all good",
                "no thanks", "no thank you", "nothing more", "that will be all"
        );

        return closingPhrases.contains(normalized);
    }

    /**
     * Checks whether the ASSISTANT message that immediately preceded the given
     * user message ended with the standard closing prompt.
     *
     * <p>This is the guard that prevents false auto-resolves when the user's
     * short reply ("no", "done", "thanks") was actually answering some other
     * assistant question rather than confirming they're finished. The check
     * normalizes the message content to lowercase and examines the trailing
     * text for known closing prompt patterns.</p>
     *
     * @param conversation the conversation to check
     * @param beforeId     the id of the user message that follows the target assistant message
     * @return {@code true} if the assistant message preceding {@code beforeId} was a closing prompt
     */
    private boolean lastAssistantMessageWasClosingPrompt(ChatConversation conversation, long beforeId) {
        return messageRepository
                .findFirstByConversationIdAndRoleAndIdLessThanOrderByIdDesc(conversation.getId(), ChatMessageRole.ASSISTANT, beforeId)
                .map(msg -> {
                    String normalized = msg.getContent().trim().toLowerCase();
                    return normalized.endsWith("is there anything else i can help you with?")
                            || normalized.endsWith("anything else i can help you with?")
                            || normalized.endsWith("anything else you need help with?")
                            || normalized.endsWith("anything else i can do for you?")
                            || normalized.endsWith("is there anything else you need?");
                })
                .orElse(false);
    }

    /**
     * Generates a unique chat ID in the format {@code CHAT-yyyy-MM-XXXX}.
     *
     * <p>The prefix includes the current year and month for human readability.
     * The suffix is a 4-character random alphanumeric string derived from a
     * UUID. A retry loop ensures uniqueness against the database by checking
     * {@link ChatConversationRepository#existsByChatId(String)} before
     * returning the result.</p>
     *
     * @return a unique chat ID string
     */
    private String generateChatId() {
        String prefix = "CHAT-" + YearMonth.now(clock)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String suffix;
        do {
            suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        } while (conversationRepository.existsByChatId(prefix + "-" + suffix));
        return prefix + "-" + suffix;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Guest messages are retrieved from the in-memory store and mapped to
     * DTOs with synthetic sequential IDs. Thread safety is ensured by taking
     * a snapshot of the message list.</p>
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

    // ========================= Guest History & Resolve =========================

    @Override
    public void resolveGuestConversation(String sessionId) {
        guestMessageStore.remove(sessionId);
        chatMemoryConfig.clearMemory("guest-" + sessionId);
        log.debug("Guest conversation and memory cleared for session {}", sessionId);
    }

    /**
     * Periodically scans for inactive conversations and resolves them.
     *
     * <p>Runs every 5 minutes (with a 60-second initial delay). Finds all
     * ACTIVE conversations with no activity for 30 or more minutes, adds a
     * SYSTEM message recording the auto-resolution, and marks them as RESOLVED.
     * This prevents resource leakage from abandoned conversations and ensures
     * chat memory windows are properly evicted.</p>
     */
    @Scheduled(fixedRate = 300_000, initialDelay = 60_000)
    public void autoResolveInactiveConversations() {
        Instant cutoff = Instant.now(clock).minus(Duration.ofMinutes(30));
        List<ChatConversation> inactive = conversationRepository
                .findByStatusAndUpdatedAtBefore(ConversationStatus.ACTIVE, cutoff);

        if (inactive.isEmpty()) {
            return;
        }

        log.info("[AUTO-RESOLVE] Found {} inactive conversations to resolve", inactive.size());

        for (ChatConversation conv : inactive) {
            try {
                conv.setStatus(ConversationStatus.RESOLVED);
                conv.setResolvedAt(Instant.now(clock));
                conversationRepository.save(conv);

                saveMessage(conv, ChatMessageRole.SYSTEM, "Conversation auto-resolved due to inactivity.");
                chatMemoryConfig.clearMemory(conv.getChatId());
                log.debug("[AUTO-RESOLVE] Resolved conversation {} and cleared memory", conv.getChatId());
            } catch (Exception e) {
                log.error("[AUTO-RESOLVE] Failed to resolve conversation {}: {}", conv.getChatId(), e.getMessage());
            }
        }
    }

    // ========================= Auto-Resolve Scheduled Task =========================

    private void saveGuestMessage(String sessionId, String role, String content) {
        guestMessageStore.computeIfAbsent(sessionId, k -> java.util.Collections.synchronizedList(new ArrayList<>()))
                .add(new GuestMessage(role, content, Instant.now(clock)));
    }

    // ========================= Guest In-Memory Helpers =========================

    /**
     * Lightweight record for guest messages held in memory.
     */
    public record GuestMessage(String role, String content, Instant createdAt) {
    }
}
