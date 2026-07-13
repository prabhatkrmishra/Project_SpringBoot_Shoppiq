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
import com.pkmprojects.shoppiq.exception.AiConversationNotFoundException;
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

import java.util.List;
import java.util.Map;

/**
 * Admin REST controller for managing and reviewing AI chat conversations.
 *
 * <p>
 * Provides paginated listing with search/filter capabilities, a detail view for
 * individual conversations, and mutation endpoints for deleting messages,
 * deleting conversations, and marking conversations as resolved. All endpoints
 * require {@code ROLE_ADMIN}.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code GET /api/admin/ai-chats} — paginated conversation list with search and status filter</li>
 *   <li>{@code GET /api/admin/ai-chats/{chatId}} — full conversation detail with all messages</li>
 *   <li>{@code DELETE /api/admin/ai-chats/{chatId}} — delete a conversation and all its messages</li>
 *   <li>{@code DELETE /api/admin/ai-chats/messages/{messageId}} — delete a single message</li>
 *   <li>{@code PATCH /api/admin/ai-chats/{chatId}/resolve} — mark a conversation as resolved</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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

    /**
     * Constructs a new {@code AdminAiChatController} with the required dependencies.
     *
     * @param conversationRepository repository for conversation queries
     * @param messageRepository      repository for message counting
     * @param pagination             page size configuration
     */
    public AdminAiChatController(ChatConversationRepository conversationRepository,
                                  ChatMessageRepository messageRepository,
                                  PaginationProperties pagination) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.pagination = pagination;
    }

    /**
     * Returns a paginated list of all AI conversations with optional search and status filter.
     *
     * <p>
     * When a {@code query} parameter is provided, searches across chat ID, title,
     * and username. When a {@code status} parameter is provided, filters by
     * conversation status. Both can be combined.
     *
     * @param query  optional search term (case-insensitive partial match)
     * @param status optional status filter ({@code ACTIVE} or {@code RESOLVED})
     * @param page   zero-based page index (default 0)
     * @param size   page size (default 20, capped by {@link PaginationProperties})
     * @return paginated list of conversation summaries
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
     * @param messageId the message's database ID
     * @return 204 No Content on success
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
     * @param chatId the public conversation identifier
     * @return 204 No Content on success
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
     * @param chatId the public conversation identifier
     * @return 204 No Content on success
     */
    @Transactional
    @PatchMapping("/{chatId}/resolve")
    public ResponseEntity<Void> resolveConversation(@PathVariable String chatId) {
        ChatConversation conv = conversationRepository.findByChatId(chatId)
            .orElseThrow(() -> AiConversationNotFoundException.chatId(chatId));

        conv.setStatus(ConversationStatus.RESOLVED);
        conv.setResolvedAt(java.time.Instant.now());
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
     * Returns the full detail of a single AI conversation, including all messages.
     *
     * @param chatId the public conversation identifier
     * @return the conversation detail with messages in chronological order
     * @throws AiConversationNotFoundException if no conversation matches the given chat ID
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
