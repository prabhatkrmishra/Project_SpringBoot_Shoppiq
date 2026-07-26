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
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.AiConversationNotFoundException;
import com.pkmprojects.shoppiq.repository.UserRepository;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private final ChatMemoryProvider chatMemoryProvider;
    private final ChatMemoryConfig chatMemoryConfig;
    private final ShoppiqTools shoppiqTools;
    private final ContentRetriever contentRetriever;
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ModelResolutionService modelResolutionService;
    private final SystemPromptProvider authenticatedPrompt;
    private final SystemPromptProvider guestPrompt;

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
     * @param chatMemoryProvider     provides per-conversation memory windows
     * @param chatMemoryConfig       manages chat memory lifecycle (clear on resolve)
     * @param shoppiqTools           tool methods available to the AI (product search, orders, etc.)
     * @param contentRetriever       RAG content retriever for product context
     * @param conversationRepository persistence for conversations
     * @param messageRepository      persistence for messages
     * @param userRepository         user lookups (unused directly but retained for future admin features)
     * @param modelResolutionService central service for resolving model names to model instances
     * @param authenticatedPrompt    system prompt for logged-in users
     * @param guestPrompt            system prompt for guest sessions
     */
    public ChatServiceImpl(ChatMemoryProvider chatMemoryProvider,
                           ChatMemoryConfig chatMemoryConfig,
                           ShoppiqTools shoppiqTools,
                           ContentRetriever contentRetriever,
                           ChatConversationRepository conversationRepository,
                           ChatMessageRepository messageRepository,
                           UserRepository userRepository,
                           ModelResolutionService modelResolutionService,
                           @Qualifier("authenticatedSystemPrompt") SystemPromptProvider authenticatedPrompt,
                           @Qualifier("guestSystemPrompt") SystemPromptProvider guestPrompt) {
        this.chatMemoryProvider = chatMemoryProvider;
        this.chatMemoryConfig = chatMemoryConfig;
        this.shoppiqTools = shoppiqTools;
        this.contentRetriever = contentRetriever;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.modelResolutionService = modelResolutionService;
        this.authenticatedPrompt = authenticatedPrompt;
        this.guestPrompt = guestPrompt;
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

        String systemPrompt = authenticatedPrompt.buildPrompt(conv.getChatId(), user);
        ChatModel resolvedModel = modelResolutionService.resolveChatModel(model);

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
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("timeout")) {
                throw AiAssistantException.timeout("AI assistant timed out. Please try again.");
            }
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

        String systemPrompt = authenticatedPrompt.buildPrompt(conv.getChatId(), user);
        StringBuilder fullResponse = new StringBuilder();
        StreamingChatModel resolvedStreamingModel = modelResolutionService.resolveStreamingChatModel(model);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public ConversationStatus getConversationStatus(String chatId, User user) {
        ChatConversation conv = resolveConversationEntity(chatId, user);
        return conv.getStatus();
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

        String systemPrompt = guestPrompt.buildPrompt(null, null);
        ChatModel resolvedModel = modelResolutionService.resolveChatModel(model);

        ShoppiqAssistant proxy = AiServices.builder(ShoppiqAssistant.class)
                .chatModel(resolvedModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(memoryId -> systemPrompt)
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

        String systemPrompt = guestPrompt.buildPrompt(null, null);
        StringBuilder fullResponse = new StringBuilder();
        StreamingChatModel resolvedStreamingModel = modelResolutionService.resolveStreamingChatModel(model);

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
                    saveGuestMessage(sessionId, "ASSISTANT", "I'm sorry, an error occurred. Please try again.");
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
            throw AiAccessDeniedException.forConversation(chatId);
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
     * Auto-resolution is triggered ONLY when ALL the following hold:
     * <ul>
     *   <li>The conversation has at least {@code resolveThreshold} user messages</li>
     *   <li>The immediately preceding ASSISTANT message was itself a closing
     *       prompt (e.g. ended by asking "Is there anything else I can help
     *       you with?") — this ensures a short reply like "no"/"done"/"thanks"
     *       is only interpreted as a closing signal when it's actually answering
     *       that question, and not when it's answering some unrelated question
     *       the assistant asked (e.g. "Do you want me to filter by size too?"
     *       → "no")</li>
     *   <li>The user's ENTIRE message (after trimming, lowercasing, and
     *       stripping trailing punctuation) exactly matches one of the known
     *       closing phrases — not just contains one, to avoid false positives
     *       like "no, show me something else" or "nah I meant the blue one"</li>
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

        if (!lastAssistantMessageWasClosingPrompt(conversation)) {
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
     * Checks whether the most recent ASSISTANT message in the conversation
     * ended with the standard closing prompt, e.g.
     * "Is there anything else I can help you with?"
     *
     * <p>
     * This is the guard that prevents false auto-resolves when the user's
     * short reply ("no", "done", "thanks") was actually answering some other
     * assistant question rather than confirming they're finished.
     *
     * @param conversation the conversation to check
     * @return {@code true} if the last assistant message was a closing prompt
     */
    private boolean lastAssistantMessageWasClosingPrompt(ChatConversation conversation) {
        return messageRepository
                .findTopByConversationIdAndRoleOrderByCreatedAtDesc(conversation.getId(), ChatMessageRole.ASSISTANT)
                .map(msg -> {
                    String normalized = msg.getContent().trim().toLowerCase();
                    return normalized.endsWith("is there anything else i can help you with?")
                            || normalized.endsWith("anything else i can help you with?")
                            || normalized.endsWith("anything else you need help with?");
                })
                .orElse(false);
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
