package com.pkmprojects.shoppiq.aiservice.controller;

import com.pkmprojects.shoppiq.aiservice.dto.ChatMessageDto;
import com.pkmprojects.shoppiq.aiservice.dto.ChatRequest;
import com.pkmprojects.shoppiq.aiservice.dto.ChatResponse;
import com.pkmprojects.shoppiq.aiservice.entity.ChatConversation;
import com.pkmprojects.shoppiq.aiservice.exception.AiServiceUnavailableException;
import com.pkmprojects.shoppiq.aiservice.service.ChatService;
import com.pkmprojects.shoppiq.entity.User;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for authenticated AI chat conversations.
 *
 * <p>
 * Provides endpoints for creating conversations, sending messages (with
 * optional streaming), listing conversation history, and resolving conversations.
 * All endpoints require a valid JWT and {@code ROLE_CUSTOMER} or {@code ROLE_ADMIN}.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code POST /api/ai/chat} — create a new conversation and send the first message</li>
 *   <li>{@code POST /api/ai/chat/{chatId}} — send a message to an existing conversation</li>
 *   <li>{@code GET /api/ai/chat/conversations} — list all conversations for the authenticated user</li>
 *   <li>{@code GET /api/ai/chat/{chatId}/messages} — retrieve full message history</li>
 *   <li>{@code DELETE /api/ai/chat/{chatId}} — mark a conversation as resolved</li>
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

    @Autowired(required = false)
    private ChatService chatService;

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
     * <p>
     * Generates a unique chat ID, persists the user message, invokes the AI model,
     * persists the assistant response, and returns the full conversation with all
     * messages. The {@code model} field in the request body selects which LLM to
     * use (omitting it falls back to the default model).
     *
     * @param user    the authenticated user (injected by Spring Security)
     * @param request the chat request containing the user message and optional model
     * @return the new conversation ID and full message history
     */
    @PostMapping
    public ResponseEntity<?> createAndChat(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChatRequest request) {
        checkServiceAvailable();

        ChatConversation conv = chatService.createConversation(user);
        String response = chatService.chat(request.message(), conv.getChatId(), user, request.model());
        List<ChatMessageDto> messages = chatService.getMessages(conv.getChatId(), user);

        return ResponseEntity.ok(new ChatResponse(conv.getChatId(), messages));
    }

    /**
     * Sends a message to an existing conversation.
     *
     * <p>
     * Validates conversation ownership, persists the user message, invokes the AI
     * model, persists the assistant response, and returns the updated conversation.
     * Throws if the conversation has been resolved.
     *
     * @param user    the authenticated user (injected by Spring Security)
     * @param chatId  the public conversation identifier
     * @param request the chat request containing the user message and optional model
     * @return the updated conversation ID and full message history
     */
    @PostMapping("/{chatId}")
    public ResponseEntity<?> chat(
            @AuthenticationPrincipal User user,
            @PathVariable String chatId,
            @Valid @RequestBody ChatRequest request) {
        checkServiceAvailable();

        chatService.chat(request.message(), chatId, user, request.model());
        List<ChatMessageDto> messages = chatService.getMessages(chatId, user);

        return ResponseEntity.ok(new ChatResponse(chatId, messages));
    }

    /**
     * Returns all conversations for the authenticated user, ordered by most recently updated.
     *
     * @param user the authenticated user (injected by Spring Security)
     * @return list of conversation summaries
     */
    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(
            @AuthenticationPrincipal User user) {
        checkServiceAvailable();

        return ResponseEntity.ok(chatService.getConversations(user));
    }

    /**
     * Returns the full message history for a specific conversation.
     *
     * @param user   the authenticated user (injected by Spring Security)
     * @param chatId the public conversation identifier
     * @return list of messages in chronological order
     */
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<?> getMessages(
            @AuthenticationPrincipal User user,
            @PathVariable String chatId) {
        checkServiceAvailable();

        return ResponseEntity.ok(chatService.getMessages(chatId, user));
    }

    /**
     * Marks a conversation as resolved, preventing further messages.
     *
     * @param user   the authenticated user (injected by Spring Security)
     * @param chatId the public conversation identifier
     * @return 204 No Content on success
     */
    @DeleteMapping("/{chatId}")
    public ResponseEntity<?> resolveConversation(
            @AuthenticationPrincipal User user,
            @PathVariable String chatId) {
        checkServiceAvailable();

        chatService.resolveConversation(chatId, user);
        return ResponseEntity.noContent().build();
    }
}
