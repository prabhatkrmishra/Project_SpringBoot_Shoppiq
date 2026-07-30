package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.aiservice.dto.ChatMessageDto;
import com.pkmprojects.shoppiq.aiservice.dto.ConversationSummary;
import com.pkmprojects.shoppiq.aiservice.entity.ChatConversation;
import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;
import com.pkmprojects.shoppiq.entity.user.User;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Service interface for the AI chat assistant.
 *
 * <p>This interface defines the complete contract for AI chat operations,
 * covering both authenticated and guest conversation workflows. It provides
 * methods for sending messages (synchronous and streaming), conversation
 * lifecycle management (creation, listing, resolution), and message history
 * retrieval. The service handles conversation ownership validation, chat
 * memory management, AI model invocation, and message persistence.</p>
 *
 * <p>Implementations of this interface orchestrate the LangChain4j AI
 * proxy, RAG content retrieval, tool execution, and Spring Data persistence
 * to deliver a complete conversational AI experience for the Shoppiq
 * e-commerce platform.</p>
 *
 * @author prabhatkrmishra
 * @see ChatServiceImpl
 * @since 1.0.0
 */
public interface ChatService {

    /**
     * Sends a message to the AI assistant within an existing conversation.
     *
     * <p>This method validates conversation ownership, checks that the conversation
     * is not resolved, persists both the user message and assistant response,
     * and optionally auto-resolves the conversation if the user indicates they
     * are done. The AI model is invoked with the conversation's system prompt,
     * chat memory, tool access, and RAG content retriever.</p>
     *
     * @param userMessage the user's message text
     * @param chatId      the public conversation identifier
     * @param user        the authenticated user requesting the chat
     * @param model       the AI model to use (null = default model)
     * @return the AI assistant's response text
     */
    String chat(String userMessage, String chatId, User user, String model);

    /**
     * Sends a message and returns the AI response as a reactive stream.
     *
     * <p>Identical to {@link #chat(String, String, User, String)} but returns
     * tokens incrementally via {@link Flux} for real-time rendering in the UI.
     * The full response is assembled in memory and persisted to the database
     * after the stream completes. Error handling is included to persist a
     * fallback error message if the stream fails.</p>
     *
     * @param userMessage the user's message text
     * @param chatId      the public conversation identifier
     * @param user        the authenticated user requesting the chat
     * @param model       the AI model to use (null = default model)
     * @return a {@link Flux} of response tokens
     */
    Flux<String> chatStream(String userMessage, String chatId, User user, String model);

    /**
     * Creates a new conversation for the given user.
     *
     * <p>Generates a unique chat ID in the format {@code CHAT-yyyy-MM-XXXX}
     * where {@code XXXX} is a random alphanumeric suffix. The new conversation
     * is persisted with {@link ConversationStatus#ACTIVE} status and a default
     * title of "New Conversation" that is updated after the first user message.</p>
     *
     * @param user the authenticated user who owns the conversation
     * @return the newly created conversation entity
     */
    ChatConversation createConversation(User user);

    /**
     * Returns a summary list of all conversations for the given user.
     *
     * <p>Each summary includes the conversation's public chat ID, title,
     * status, user message count, and timestamps. Summaries are ordered
     * by most recently updated first, suitable for rendering in the sidebar
     * conversation list.</p>
     *
     * @param user the authenticated user
     * @return list of conversation summaries ordered by most recently updated
     */
    List<ConversationSummary> getConversations(User user);

    /**
     * Returns the full message history for a specific conversation.
     *
     * <p>Validates that the conversation belongs to the given user before
     * returning the messages. Messages are returned in chronological order
     * with all roles (USER, ASSISTANT, SYSTEM, TOOL) included.</p>
     *
     * @param chatId the public conversation identifier
     * @param user   the authenticated user (used for ownership validation)
     * @return list of messages in chronological order
     */
    List<ChatMessageDto> getMessages(String chatId, User user);

    /**
     * Returns the current status of a conversation.
     *
     * <p>Validates that the conversation belongs to the given user before
     * returning its status. The status indicates whether the conversation
     * is still active or has been resolved.</p>
     *
     * @param chatId the public conversation identifier
     * @param user   the authenticated user (used for ownership validation)
     * @return the current conversation status
     */
    ConversationStatus getConversationStatus(String chatId, User user);

    /**
     * Marks a conversation as resolved and prevents further messages.
     *
     * <p>Sets the conversation status to {@link ConversationStatus#RESOLVED},
     * records the resolution timestamp, and appends a SYSTEM message to the
     * conversation history. The in-memory chat window is also cleared to free
     * resources. This operation is idempotent; resolving an already-resolved
     * conversation is a no-op.</p>
     *
     * @param chatId the public conversation identifier
     * @param user   the authenticated user (used for ownership validation)
     */
    void resolveConversation(String chatId, User user);

    /**
     * Sends a message as a guest user (unauthenticated).
     *
     * <p>Guest conversations are tracked by a session cookie rather than user
     * identity. Guest sessions have access to product search only via the RAG
     * pipeline; no tool access (orders, cart, reviews) is provided. Conversations
     * are stored in memory and are not persisted to the database.</p>
     *
     * @param userMessage the guest's message text
     * @param sessionId   the guest session UUID (from cookie)
     * @param model       the AI model to use (null = default model)
     * @return the AI assistant's response text
     */
    String guestChat(String userMessage, String sessionId, String model);

    /**
     * Sends a guest message and returns the response as a reactive stream.
     *
     * <p>Streaming variant of {@link #guestChat(String, String, String)} that
     * returns tokens incrementally via {@link Flux} for real-time rendering.
     * No tool access is provided for guest sessions.</p>
     *
     * @param userMessage the guest's message text
     * @param sessionId   the guest session UUID (from cookie)
     * @param model       the AI model to use (null = default model)
     * @return a {@link Flux} of response tokens
     */
    Flux<String> guestChatStream(String userMessage, String sessionId, String model);

    /**
     * Returns the full message history for a guest conversation.
     *
     * <p>Guest messages are stored in memory (not persisted to the database)
     * and are returned with synthetic sequential IDs. Messages are ordered
     * chronologically.</p>
     *
     * @param sessionId the guest session UUID (from cookie)
     * @return list of messages in chronological order
     */
    List<ChatMessageDto> getGuestMessages(String sessionId);

    /**
     * Marks a guest conversation as resolved.
     *
     * <p>Clears the in-memory message store for the session and evicts
     * the chat memory window. This operation is irreversible; guest
     * messages are not persisted to the database.</p>
     *
     * @param sessionId the guest session UUID (from cookie)
     */
    void resolveGuestConversation(String sessionId);
}
