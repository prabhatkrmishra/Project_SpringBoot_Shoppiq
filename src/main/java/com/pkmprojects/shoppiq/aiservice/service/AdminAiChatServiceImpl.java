package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.aiservice.dto.ChatMessageDto;
import com.pkmprojects.shoppiq.aiservice.dto.admin.AiChatLogDetailDto;
import com.pkmprojects.shoppiq.aiservice.repository.projection.ConversationMessageCount;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Default implementation of {@link AdminAiChatService}.
 *
 * <p><b>How AI fits:</b> Performs admin-specific operations over AI
 * conversations: paginated listing (with optional search query and status
 * filter), full message transcript retrieval, message deletion, conversation
 * deletion, and resolution. Uses batch-count queries to efficiently compute
 * per-conversation message counts.</p>
 *
 * <p><b>Pattern used:</b> Facade over {@link ChatConversationRepository}
 * and {@link ChatMessageRepository} with transactional read/write methods.
 * Maps JPA entities to admin DTOs ({@link com.pkmprojects.shoppiq.aiservice.dto.admin.AiChatLogDto},
 * {@link com.pkmprojects.shoppiq.aiservice.dto.admin.AiChatLogDetailDto}).</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Service
class AdminAiChatServiceImpl implements AdminAiChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;

    /**
     * Constructs the admin service with required repositories.
     *
     * @param conversationRepository conversation data access
     * @param messageRepository      message data access
     */
    AdminAiChatServiceImpl(ChatConversationRepository conversationRepository,
                           ChatMessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Routes through the appropriate repository method based on the presence
     * of search query and/or status filter. Batch-counts user messages to
     * populate the message count field without N+1 queries.
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
     * <p>
     * Loads the conversation and all its messages, mapping them to DTOs
     * with role labels, tool names, and timestamps.
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
     * <p>
     * Returns {@code false} if no message exists with the given ID.
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
     * <p>
     * Cascading delete — removes all child messages before deleting the
     * conversation entity itself.
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
     * <p>
     * Updates the conversation status to RESOLVED, sets the resolution
     * timestamp, and appends a SYSTEM message to the conversation history.
     */
    @Override
    @Transactional
    public void resolveConversation(String chatId) {
        ChatConversation conv = conversationRepository.findByChatId(chatId)
                .orElseThrow(() -> AiConversationNotFoundException.chatId(chatId));

        conv.setStatus(ConversationStatus.RESOLVED);
        conv.setResolvedAt(Instant.now());
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
     * @param conv       the conversation entity
     * @param msgCounts  map of conversation ID to user-message count
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
