package com.pkmprojects.shoppiq.aiservice.controller;

import com.pkmprojects.shoppiq.aiservice.dto.ChatRequest;
import com.pkmprojects.shoppiq.aiservice.service.ChatService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for guest (unauthenticated) AI chat conversations.
 *
 * <p>
 * Provides endpoints for guest users to interact with the AI assistant without
 * an account. Guest sessions are tracked via a {@code GUEST_SESSION} cookie.
 * Guest conversations have no tool access — only product catalog search via the
 * RAG retrieval pipeline.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code POST /api/ai/guest} — send a message and receive the AI response</li>
 *   <li>{@code GET /api/ai/guest/{sessionId}/messages} — retrieve full message history</li>
 *   <li>{@code DELETE /api/ai/guest/{sessionId}} — clear a guest conversation</li>
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

    @Autowired(required = false)
    private ChatService chatService;

    @PostConstruct
    void logInit() {
        log.debug("[AI-INIT] AiGuestChatController registered — serviceAvailable={}", chatService != null);
    }

    /**
     * Sends a message as a guest user and receives the AI response.
     *
     * <p>
     * If no {@code GUEST_SESSION} cookie is present, a new session UUID is generated
     * and set as an HttpOnly cookie with a 24-hour expiry. The session ID is returned
     * in the response body for the frontend to store in {@code localStorage}.
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

        if (chatService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "AI service is not available. Check NVIDIA_API_KEY configuration."));
        }

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
     * @param sessionId the guest session UUID (from cookie)
     * @return list of messages in chronological order
     */
    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<?> getGuestMessages(@PathVariable String sessionId) {
        if (chatService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "AI service is not available."));
        }

        return ResponseEntity.ok(chatService.getGuestMessages(sessionId));
    }

    /**
     * Clears a guest conversation's in-memory message store.
     *
     * @param sessionId the guest session UUID (from cookie)
     * @return 204 No Content on success
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> resolveGuestConversation(@PathVariable String sessionId) {
        if (chatService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "AI service is not available."));
        }

        chatService.resolveGuestConversation(sessionId);
        return ResponseEntity.noContent().build();
    }
}
