package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.aiservice.dto.ChatMessageDto;
import com.pkmprojects.shoppiq.aiservice.dto.admin.AiChatLogDetailDto;
import com.pkmprojects.shoppiq.aiservice.dto.admin.AiChatLogDto;
import com.pkmprojects.shoppiq.aiservice.entity.ChatConversation;
import com.pkmprojects.shoppiq.aiservice.entity.ChatMessage;
import com.pkmprojects.shoppiq.aiservice.enums.ChatMessageRole;
import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;
import com.pkmprojects.shoppiq.aiservice.repository.ChatConversationRepository;
import com.pkmprojects.shoppiq.aiservice.repository.ChatMessageRepository;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.exception.general.aiservice.AiConversationNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link AdminAiChatService}.
 *
 * <p>This service performs admin-specific operations over AI conversations
 * with transactional read/write methods. It routes through the appropriate
 * repository method based on the presence of search query and/or status
 * filter, and batch-counts user messages to populate the message count
 * field without N+1 query issues.</p>
 *
 * <p>All write operations (delete, resolve) include existence checks and
 * throw {@link AiConversationNotFoundException} if the target conversation
 * does not exist. Conversation deletion cascades to remove all child
 * messages before deleting the conversation entity.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Service
class AdminAiChatServiceImpl implements AdminAiChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final Clock clock;

    /**
     * Constructs the admin service with required repositories.
     *
     * <p>The clock dependency is used for deterministic timestamp generation
     * during conversation resolution, supporting testing and audit logging.</p>
     *
     * @param conversationRepository conversation data access
     * @param messageRepository      message data access
     * @param clock                  clock for deterministic time
     */
    AdminAiChatServiceImpl(ChatConversationRepository conversationRepository,
                           ChatMessageRepository messageRepository,
                           Clock clock) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.clock = clock;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Routes through the appropriate repository method based on the presence
     * of search query and/or status filter. Batch-counts user messages via
     * {@link ChatMessageRepository#countByConversationIdsAndRoleBatch} to
     * populate the message count field without N+1 query issues.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiChatLogDto> getConversations(String query, ConversationStatus status, PageRequest pageable) {
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
        var counts = messageRepository
                .countByConversationIdsAndRoleBatch(convIds, ChatMessageRole.USER);
        Map<Long, Long> msgCounts = new HashMap<>();
        for (var row : counts) {
            msgCounts.put((Long) row[0], (Long) row[1]);
        }

        return PageResponse.of(conversations, conv -> toLogDto(conv, msgCounts));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Loads the conversation and all its messages, mapping them to DTOs
     * with role labels, tool names, and timestamps. Guest conversations
     * display "Guest" as the user name with null user ID and email fields.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public AiChatLogDetailDto getConversationDetail(String chatId) {
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

        return new AiChatLogDetailDto(
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
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code false} if no message exists with the given ID.
     * The deletion is immediate and does not affect the parent conversation.</p>
     */
    @Override
    @Transactional
    public boolean deleteMessage(Long messageId) {
        if (!messageRepository.existsById(messageId)) {
            return false;
        }
        messageRepository.deleteById(messageId);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Performs a cascading delete: removes all child messages before deleting
     * the conversation entity itself. Throws if the conversation does not exist.</p>
     */
    @Override
    @Transactional
    public void deleteConversation(String chatId) {
        ChatConversation conv = conversationRepository.findByChatId(chatId)
                .orElseThrow(() -> AiConversationNotFoundException.chatId(chatId));

        messageRepository.deleteByConversationId(conv.getId());
        conversationRepository.delete(conv);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Updates the conversation status to RESOLVED, sets the resolution
     * timestamp, and appends a SYSTEM message recording the admin-initiated
     * resolution. Throws if the conversation does not exist.</p>
     */
    @Override
    @Transactional
    public void resolveConversation(String chatId) {
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
    }

    /**
     * Maps a {@link ChatConversation} entity and pre-computed message counts
     * to an {@link AiChatLogDto}.
     *
     * <p>Extracts user identification fields (ID, name, email) from the
     * conversation's user relationship, falling back to "Guest" for
     * unauthenticated sessions. The message count is sourced from the
     * pre-computed map to avoid additional database queries.</p>
     *
     * @param conv      the conversation entity
     * @param msgCounts map of conversation ID to user-message count
     * @return the admin DTO
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
