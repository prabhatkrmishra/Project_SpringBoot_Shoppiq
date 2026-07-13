package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.aiservice.dto.ChatMessageDto;
import com.pkmprojects.shoppiq.aiservice.dto.ConversationSummary;
import com.pkmprojects.shoppiq.aiservice.entity.ChatConversation;
import com.pkmprojects.shoppiq.entity.User;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Service interface for the AI chat assistant.
 *
 * <p>
 * Provides both authenticated and guest chat functionality, conversation
 * lifecycle management, and message persistence. Implementations are
 * profile-gated to {@code ai-enabled} — when the profile is inactive,
 * no bean is created and the AI endpoints return 404.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Send user messages to the AI model and persist the response</li>
 *   <li>Manage conversation creation, listing, and resolution</li>
 *   <li>Enforce conversation ownership (users can only access their own)</li>
 *   <li>Support guest sessions via cookie-based session identifiers</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @see ChatServiceImpl
 * @since 1.0.0
 */
public interface ChatService {

    /**
     * Sends a message to the AI assistant within an existing conversation.
     *
     * <p>
     * The method validates conversation ownership, checks that the conversation
     * is not resolved, persists both the user message and assistant response,
     * and optionally auto-resolves the conversation if the user indicates they
     * are done.
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
     * <p>
     * Identical to {@link #chat(String, String, User, String)} but returns tokens
     * incrementally via {@link Flux} for real-time rendering in the UI.
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
     * <p>
     * Generates a unique chat ID in the format {@code CHAT-yyyy-MM-XXXX} and
     * persists a new {@link ChatConversation} with {@code ACTIVE} status.
     *
     * @param user the authenticated user who owns the conversation
     * @return the newly created conversation entity
     */
    ChatConversation createConversation(User user);

    /**
     * Returns a summary list of all conversations for the given user.
     *
     * @param user the authenticated user
     * @return list of conversation summaries ordered by most recently updated
     */
    List<ConversationSummary> getConversations(User user);

    /**
     * Returns the full message history for a specific conversation.
     *
     * @param chatId the public conversation identifier
     * @param user   the authenticated user (used for ownership validation)
     * @return list of messages in chronological order
     */
    List<ChatMessageDto> getMessages(String chatId, User user);

    /**
     * Marks a conversation as resolved and prevents further messages.
     *
     * <p>
     * Sets the conversation status to {@link com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus#RESOLVED},
     * records the resolution timestamp, and appends a system message.
     *
     * @param chatId the public conversation identifier
     * @param user   the authenticated user (used for ownership validation)
     */
    void resolveConversation(String chatId, User user);

    /**
     * Sends a message as a guest user (unauthenticated).
     *
     * <p>
     * Guests have access to product search only — no order, cart, or review
     * tools. Conversations are tracked by a session cookie rather than user identity.
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
     * @param userMessage the guest's message text
     * @param sessionId   the guest session UUID (from cookie)
     * @param model       the AI model to use (null = default model)
     * @return a {@link Flux} of response tokens
     */
    Flux<String> guestChatStream(String userMessage, String sessionId, String model);

    /**
     * Returns the full message history for a guest conversation.
     *
     * @param sessionId the guest session UUID (from cookie)
     * @return list of messages in chronological order
     */
    List<ChatMessageDto> getGuestMessages(String sessionId);

    /**
     * Marks a guest conversation as resolved.
     *
     * @param sessionId the guest session UUID (from cookie)
     */
    void resolveGuestConversation(String sessionId);
}
