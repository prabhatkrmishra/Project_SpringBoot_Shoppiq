package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.aiservice.dto.ChatMessageDto;
import com.pkmprojects.shoppiq.aiservice.dto.admin.AiChatLogDetailDto;
import com.pkmprojects.shoppiq.aiservice.dto.admin.AiChatLogDto;
import com.pkmprojects.shoppiq.aiservice.entity.ChatConversation;
import com.pkmprojects.shoppiq.aiservice.entity.ChatMessage;
import com.pkmprojects.shoppiq.aiservice.enums.ChatMessageRole;
import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;
import com.pkmprojects.shoppiq.aiservice.repository.ChatConversationRepository;
import com.pkmprojects.shoppiq.aiservice.repository.ChatMessageRepository;
import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.exception.general.aiservice.AiConversationNotFoundException;
import jakarta.validation.constraints.Min;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Admin REST controller for managing and reviewing AI chat conversations.
 *
 * <p>Provides paginated listing with search/filter, conversation detail, message
 * deletion, conversation deletion, and resolve marking. This controller enables
 * admins to monitor and manage AI-powered support conversations from the admin
 * dashboard. It is conditionally enabled via the shoppiq.ai.enabled property.</p>
 *
 * <p>This controller acts as the HTTP boundary for AI chat administration. It
 * directly uses repository classes for query operations and conversation
 * lifecycle management, bypassing a dedicated service layer for these admin-specific
 * operations.</p>
 *
 * <p>All endpoints require ADMIN role and are conditionally mounted under
 * /api/admin/ai-chats when the AI feature is enabled.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * GET    /api/admin/ai-chats                     — paginated list with search/status filter
 * GET    /api/admin/ai-chats/{chatId}            — full conversation detail with messages
 * DELETE /api/admin/ai-chats/{chatId}            — delete a conversation and its messages
 * DELETE /api/admin/ai-chats/messages/{messageId} — delete a single message
 * PATCH  /api/admin/ai-chats/{chatId}/resolve    — mark a conversation as resolved
 * </pre>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/api/admin/ai-chats")
@PreAuthorize("hasRole('ADMIN')")
@ConditionalOnProperty(name = "shoppiq.ai.enabled", havingValue = "true", matchIfMissing = false)
public class AdminAiChatController {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final PaginationProperties pagination;
    private final Clock clock;

    /**
     * Constructs a new {@code AdminAiChatController} with the required dependencies.
     *
     * @param conversationRepository repository for conversation queries
     * @param messageRepository      repository for message counting
     * @param pagination             page size configuration
     * @param clock                  clock for time-related operations
     */
    public AdminAiChatController(ChatConversationRepository conversationRepository,
                                 ChatMessageRepository messageRepository,
                                 PaginationProperties pagination,
                                 Clock clock) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.pagination = pagination;
        this.clock = clock;
    }

    /**
     * Returns a paginated list of all AI conversations with optional search
     * and status filter.
     *
     * <p>When a query parameter is provided, searches across chat ID, title,
     * and username. When a status parameter is provided, filters by conversation
     * status. Both filters can be combined.</p>
     *
     * @param query  optional search term (case-insensitive partial match)
     * @param status optional status filter (ACTIVE or RESOLVED)
     * @param page   zero-based page index (default 0)
     * @param size   page size (default 20, capped by the configured maximum)
     * @return 200 OK with paginated list of conversation summaries
     */
    @GetMapping
    public ResponseEntity<PageResponse<AiChatLogDto>> getConversations(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ConversationStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        size = Math.min(size, pagination.maxPageSize());
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<ChatConversation> conversations;
        if (query != null && !query.isBlank() && status != null) {
            conversations = conversationRepository.searchByQueryAndStatus(query, status, pageable);
        } else if (query != null && !query.isBlank()) {
            conversations = conversationRepository.searchByQuery(query, pageable);
        } else if (status != null) {
            conversations = conversationRepository.findByStatusOrderByUpdatedAtDesc(status, pageable);
        } else {
            conversations = conversationRepository.findAllByOrderByUpdatedAtDesc(pageable);
        }

        List<Long> convIds = conversations.getContent().stream()
                .map(ChatConversation::getId)
                .toList();
        List<Object[]> counts = messageRepository
                .countByConversationIdsAndRoleBatch(convIds, ChatMessageRole.USER);
        Map<Long, Long> msgCounts = new java.util.HashMap<>();
        for (Object[] row : counts) {
            msgCounts.put((Long) row[0], (Long) row[1]);
        }

        PageResponse<AiChatLogDto> response = PageResponse.of(conversations,
                conv -> toLogDto(conv, msgCounts));
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a single chat message by ID.
     *
     * <p>The message is permanently removed from the conversation. If the
     * message does not exist, a 404 Not Found is returned.</p>
     *
     * @param messageId the message's database ID
     * @return 204 No Content on success, 404 Not Found if not found
     */
    @Transactional
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        if (!messageRepository.existsById(messageId)) {
            return ResponseEntity.notFound().build();
        }
        messageRepository.deleteById(messageId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes an entire conversation and all its messages.
     *
     * <p>This is a cascading delete that removes the conversation record
     * and all associated messages. If the conversation does not exist,
     * an AiConversationNotFoundException is thrown.</p>
     *
     * @param chatId the public conversation identifier
     * @return 204 No Content on success
     * @throws AiConversationNotFoundException if no conversation matches the chat ID
     */
    @Transactional
    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String chatId) {
        ChatConversation conv = conversationRepository.findByChatId(chatId)
                .orElseThrow(() -> AiConversationNotFoundException.chatId(chatId));

        messageRepository.deleteByConversationId(conv.getId());
        conversationRepository.delete(conv);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marks a conversation as resolved.
     *
     * <p>Sets the conversation status to RESOLVED, records the resolution
     * timestamp, and appends a system message to the conversation history.</p>
     *
     * @param chatId the public conversation identifier
     * @return 204 No Content on success
     * @throws AiConversationNotFoundException if no conversation matches the chat ID
     */
    @Transactional
    @PatchMapping("/{chatId}/resolve")
    public ResponseEntity<Void> resolveConversation(@PathVariable String chatId) {
        ChatConversation conv = conversationRepository.findByChatId(chatId)
                .orElseThrow(() -> AiConversationNotFoundException.chatId(chatId));

        conv.setStatus(ConversationStatus.RESOLVED);
        conv.setResolvedAt(Instant.now(clock));
        conversationRepository.save(conv);

        ChatMessage systemMsg = ChatMessage.builder()
                .conversation(conv)
                .role(ChatMessageRole.SYSTEM)
                .content("Conversation resolved.")
                .build();
        messageRepository.save(systemMsg);

        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the full detail of a single AI conversation, including all
     * messages in chronological order.
     *
     * <p>The response includes conversation metadata (title, status,
     * timestamps) and the complete message history with role and content.</p>
     *
     * @param chatId the public conversation identifier
     * @return 200 OK with the conversation detail with messages
     * @throws AiConversationNotFoundException if no conversation matches the chat ID
     */
    @GetMapping("/{chatId}")
    public ResponseEntity<AiChatLogDetailDto> getConversationDetail(
            @PathVariable String chatId) {

        ChatConversation conv = conversationRepository.findByChatId(chatId)
                .orElseThrow(() -> AiConversationNotFoundException.chatId(chatId));

        var messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conv.getId())
                .stream()
                .map(msg -> new ChatMessageDto(
                        msg.getId(),
                        msg.getRole().name(),
                        msg.getContent(),
                        msg.getToolName(),
                        msg.getCreatedAt()
                ))
                .toList();

        AiChatLogDetailDto detail = new AiChatLogDetailDto(
                conv.getChatId(),
                conv.getUser() != null ? conv.getUser().getId() : null,
                conv.getUser() != null ? conv.getUser().getUsername() : "Guest",
                conv.getUser() != null ? conv.getUser().getEmail() : null,
                conv.getTitle(),
                conv.getStatus().name(),
                conv.getCreatedAt(),
                conv.getResolvedAt(),
                messages
        );

        return ResponseEntity.ok(detail);
    }

    /**
     * Converts a {@link ChatConversation} entity to an {@link AiChatLogDto} summary.
     *
     * @param conv      the conversation entity
     * @param msgCounts pre-computed message counts (avoids N+1 queries)
     * @return the DTO suitable for the admin conversation list
     */
    private AiChatLogDto toLogDto(ChatConversation conv, Map<Long, Long> msgCounts) {
        long msgCount = msgCounts.getOrDefault(conv.getId(), 0L);

        return new AiChatLogDto(
                conv.getChatId(),
                conv.getUser() != null ? conv.getUser().getId() : null,
                conv.getUser() != null ? conv.getUser().getUsername() : "Guest",
                conv.getUser() != null ? conv.getUser().getEmail() : null,
                conv.getTitle(),
                conv.getStatus().name(),
                (int) msgCount,
                conv.getCreatedAt(),
                conv.getUpdatedAt()
        );
    }
}
