package com.pkmprojects.shoppiq.aiservice.controller;

import com.pkmprojects.shoppiq.aiservice.dto.ChatMessageDto;
import com.pkmprojects.shoppiq.aiservice.dto.ChatRequest;
import com.pkmprojects.shoppiq.aiservice.dto.ChatResponse;
import com.pkmprojects.shoppiq.aiservice.entity.ChatConversation;
import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;
import com.pkmprojects.shoppiq.aiservice.exception.AiServiceUnavailableException;
import com.pkmprojects.shoppiq.aiservice.service.ChatService;
import com.pkmprojects.shoppiq.entity.user.User;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for authenticated AI chat conversations.
 *
 * <p>This controller provides the primary API for authenticated users to
 * interact with the Shoppiq AI assistant. It supports conversation creation,
 * message sending (with optional model selection), conversation history
 * retrieval, and conversation resolution. All endpoints require a valid JWT
 * and {@code ROLE_CUSTOMER} or {@code ROLE_ADMIN} security clearance.</p>
 *
 * <p>The controller delegates all business logic to {@link ChatService},
 * handling only request validation, service availability checks, and
 * response assembly. Each endpoint returns a {@link ChatResponse} containing
 * the conversation ID, full message history, and current status.</p>
 *
 * <ul>
 *     <li>{@code POST   /api/ai/chat}                    — create a new conversation and send the first message</li>
 *     <li>{@code POST   /api/ai/chat/{chatId}}            — send a message to an existing conversation</li>
 *     <li>{@code GET    /api/ai/chat/conversations}       — list all conversations for the authenticated user</li>
 *     <li>{@code GET    /api/ai/chat/{chatId}/messages}   — retrieve full message history</li>
 *     <li>{@code DELETE /api/ai/chat/{chatId}}            — mark a conversation as resolved</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/chat")
@PreAuthorize("isAuthenticated()")
@ConditionalOnProperty(name = "shoppiq.ai.enabled", havingValue = "true", matchIfMissing = false)
public class AiChatController {

    private final ChatService chatService;

    public AiChatController(@Nullable ChatService chatService) {
        this.chatService = chatService;
    }

    @PostConstruct
    void logInit() {
        log.debug("[AI-INIT] AiChatController registered — serviceAvailable={}", chatService != null);
    }

    private void checkServiceAvailable() {
        if (chatService == null) {
            throw AiServiceUnavailableException.disabled();
        }
    }

    /**
     * Creates a new conversation and sends the first message.
     *
     * <p>Generates a unique chat ID, persists the user message, invokes the
     * AI model, persists the assistant response, and returns the full
     * conversation with all messages. The {@code model} field in the request
     * body selects which LLM to use; omitting it falls back to the default
     * model.</p>
     *
     * @param user    the authenticated user (injected by Spring Security)
     * @param request the chat request containing the user message and optional model
     * @return the new conversation ID and full message history
     */
    @PostMapping
    public ResponseEntity<?> createAndChat(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody ChatRequest request) {
        checkServiceAvailable();

        ChatConversation conv = chatService.createConversation(user);
        String response = chatService.chat(request.message(), conv.getChatId(), user, request.model());
        List<ChatMessageDto> messages = chatService.getMessages(conv.getChatId(), user);
        ConversationStatus status = chatService.getConversationStatus(conv.getChatId(), user);

        return ResponseEntity.ok(new ChatResponse(conv.getChatId(), messages, status));
    }

    /**
     * Sends a message to an existing conversation.
     *
     * <p>Validates conversation ownership, persists the user message, invokes
     * the AI model, persists the assistant response, and returns the updated
     * conversation. Throws if the conversation has been resolved or if the
     * user does not own the conversation.</p>
     *
     * @param user    the authenticated user (injected by Spring Security)
     * @param chatId  the public conversation identifier
     * @param request the chat request containing the user message and optional model
     * @return the updated conversation ID and full message history
     */
    @PostMapping("/{chatId}")
    public ResponseEntity<?> chat(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable String chatId,
            @Valid @RequestBody ChatRequest request) {
        checkServiceAvailable();

        chatService.chat(request.message(), chatId, user, request.model());
        List<ChatMessageDto> messages = chatService.getMessages(chatId, user);
        ConversationStatus status = chatService.getConversationStatus(chatId, user);

        return ResponseEntity.ok(new ChatResponse(chatId, messages, status));
    }

    /**
     * Returns all conversations for the authenticated user, ordered by most recently updated.
     *
     * <p>The returned list includes conversation summaries with chat ID, title,
     * status, message count, and timestamps, suitable for rendering in the
     * sidebar conversation list.</p>
     *
     * @param user the authenticated user (injected by Spring Security)
     * @return list of conversation summaries
     */
    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(
            @AuthenticationPrincipal(expression = "user") User user) {
        checkServiceAvailable();

        return ResponseEntity.ok(chatService.getConversations(user));
    }

    /**
     * Returns the full message history for a specific conversation.
     *
     * <p>Validates that the conversation belongs to the authenticated user
     * before returning the messages. Messages are returned in chronological
     * order with all roles included.</p>
     *
     * @param user   the authenticated user (injected by Spring Security)
     * @param chatId the public conversation identifier
     * @return list of messages in chronological order
     */
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<?> getMessages(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable String chatId) {
        checkServiceAvailable();

        return ResponseEntity.ok(chatService.getMessages(chatId, user));
    }

    /**
     * Marks a conversation as resolved, preventing further messages.
     *
     * <p>Sets the conversation status to RESOLVED, records the resolution
     * timestamp, and appends a SYSTEM message. Returns 204 No Content
     * on success. The conversation remains in the database for historical
     * review.</p>
     *
     * @param user   the authenticated user (injected by Spring Security)
     * @param chatId the public conversation identifier
     * @return 204 No Content on success
     */
    @DeleteMapping("/{chatId}")
    public ResponseEntity<?> resolveConversation(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable String chatId) {
        checkServiceAvailable();

        chatService.resolveConversation(chatId, user);
        return ResponseEntity.noContent().build();
    }
}
