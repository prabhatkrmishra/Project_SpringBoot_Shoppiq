package com.pkmprojects.shoppiq.aiservice.controller;

import com.pkmprojects.shoppiq.aiservice.dto.ChatRequest;
import com.pkmprojects.shoppiq.aiservice.exception.AiServiceUnavailableException;
import com.pkmprojects.shoppiq.aiservice.service.ChatService;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for guest (unauthenticated) AI chat conversations.
 *
 * <p>This controller provides endpoints for guest users to interact with the
 * AI assistant without requiring an account. Guest sessions are tracked via
 * a {@code GUEST_SESSION} cookie with a 24-hour expiry. Guest conversations
 * have no tool access (orders, cart, reviews) and rely solely on the RAG
 * retrieval pipeline for product information.</p>
 *
 * <p>The controller manages session lifecycle: creating new session UUIDs
 * when needed, setting HttpOnly secure cookies, and returning session IDs
 * in the response body for frontend localStorage synchronization.</p>
 *
 * <ul>
 *     <li>{@code POST   /api/ai/guest}                    — send a message and receive the AI response</li>
 *     <li>{@code GET    /api/ai/guest/{sessionId}/messages} — retrieve full message history</li>
 *     <li>{@code DELETE /api/ai/guest/{sessionId}}          — clear a guest conversation</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/guest")
@ConditionalOnProperty(name = "shoppiq.ai.enabled", havingValue = "true", matchIfMissing = false)
public class AiGuestChatController {

    private final ChatService chatService;
    private final boolean secureCookie;

    public AiGuestChatController(@Nullable ChatService chatService,
                                 @Value("${app.security.secure-cookie:true}") boolean secureCookie) {
        this.chatService = chatService;
        this.secureCookie = secureCookie;
    }

    @PostConstruct
    void logInit() {
        log.debug("[AI-INIT] AiGuestChatController registered — serviceAvailable={}", chatService != null);
    }

    private void checkServiceAvailable() {
        if (chatService == null) {
            throw AiServiceUnavailableException.disabled();
        }
    }

    /**
     * Sends a message as a guest user and receives the AI response.
     *
     * <p>If no {@code GUEST_SESSION} cookie is present, a new session UUID is
     * generated and set as an HttpOnly cookie with a 24-hour expiry. The session
     * ID is also returned in the response body for the frontend to store in
     * {@code localStorage}, ensuring session persistence across page reloads.</p>
     *
     * <p>The response includes both the AI assistant's text response and the
     * session ID for frontend state management.</p>
     *
     * @param request   the chat request containing the user message and optional model
     * @param sessionId the existing guest session ID (from cookie), or {@code null} for new sessions
     * @param response  the HTTP response (used to set the session cookie)
     * @return the AI response text and the session ID
     */
    @PostMapping
    public ResponseEntity<?> guestChat(
            @RequestBody @Valid ChatRequest request,
            @CookieValue(value = "GUEST_SESSION", required = false) String sessionId,
            HttpServletResponse response) {

        checkServiceAvailable();

        boolean isNewSession = sessionId == null || sessionId.isBlank();
        if (isNewSession) {
            sessionId = UUID.randomUUID().toString();
        }

        String aiResponse = chatService.guestChat(request.message(), sessionId, request.model());

        if (isNewSession) {
            Cookie cookie = new Cookie("GUEST_SESSION", sessionId);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            cookie.setHttpOnly(true);
            cookie.setSecure(secureCookie);
            cookie.setAttribute("SameSite", "Lax");
            response.addCookie(cookie);
        }

        return ResponseEntity.ok(Map.of(
                "response", aiResponse,
                "sessionId", sessionId
        ));
    }

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
    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<?> getGuestMessages(@PathVariable String sessionId) {
        checkServiceAvailable();

        return ResponseEntity.ok(chatService.getGuestMessages(sessionId));
    }

    /**
     * Clears a guest conversation's in-memory message store.
     *
     * <p>Removes all messages for the session and evicts the chat memory
     * window. Returns 204 No Content on success. This operation is
     * irreversible; guest messages are not persisted to the database.</p>
     *
     * @param sessionId the guest session UUID (from cookie)
     * @return 204 No Content on success
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> resolveGuestConversation(@PathVariable String sessionId) {
        checkServiceAvailable();

        chatService.resolveGuestConversation(sessionId);
        return ResponseEntity.noContent().build();
    }
}
