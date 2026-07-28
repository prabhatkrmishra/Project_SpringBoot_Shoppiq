package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.aiservice.dto.admin.AiChatLogDetailDto;
import com.pkmprojects.shoppiq.aiservice.dto.admin.AiChatLogDto;
import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import org.springframework.data.domain.PageRequest;

/**
 * Service interface for admin management of AI chat conversations.
 *
 * <p><b>How AI fits:</b> Provides the admin dashboard with CRUD operations
 * over AI conversations — paginated listing with search/filter, full
 * transcript viewing, message deletion, and conversation resolution.</p>
 *
 * <p><b>Pattern used:</b> Service interface segregating admin-specific
 * AI operations from the user-facing {@link ChatService}.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface AdminAiChatService {

    /**
     * Returns a paginated list of conversations with optional search and status filter.
     */
    PageResponse<AiChatLogDto> getConversations(String query, ConversationStatus status, PageRequest pageable);

    /**
     * Returns the full detail of a single conversation including all messages.
     */
    AiChatLogDetailDto getConversationDetail(String chatId);

    /**
     * Deletes a single chat message by ID.
     */
    boolean deleteMessage(Long messageId);

    /**
     * Deletes an entire conversation and all its messages.
     */
    void deleteConversation(String chatId);

    /**
     * Marks a conversation as resolved.
     */
    void resolveConversation(String chatId);
}
