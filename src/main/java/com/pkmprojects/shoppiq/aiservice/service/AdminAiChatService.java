package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.aiservice.dto.admin.AiChatLogDetailDto;
import com.pkmprojects.shoppiq.aiservice.dto.admin.AiChatLogDto;
import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import org.springframework.data.domain.PageRequest;

/**
 * Service interface for admin management of AI chat conversations.
 *
 * <p>This interface defines the contract for administrative operations over
 * AI chat conversations, including paginated listing with search and status
 * filtering, full conversation detail retrieval, message deletion, conversation
 * deletion, and manual conversation resolution. These operations are used by
 * the admin dashboard for monitoring and managing AI chat activity.</p>
 *
 * <p>All operations are designed for administrative use and do not enforce
 * conversation ownership restrictions, allowing admins to access any
 * conversation in the system.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface AdminAiChatService {

    /**
     * Returns a paginated list of conversations with optional search and status filter.
     *
     * <p>Supports free-text search across chat ID, title, and username, with
     * optional status filtering. Results are ordered by most recently updated.
     * User message counts are batch-loaded to avoid N+1 query issues.</p>
     *
     * @param query    optional search term (null to skip search filtering)
     * @param status   optional status filter (null to include all statuses)
     * @param pageable pagination parameters
     * @return paginated list of conversation log DTOs
     */
    PageResponse<AiChatLogDto> getConversations(String query, ConversationStatus status, PageRequest pageable);

    /**
     * Returns the full detail of a single conversation including all messages.
     *
     * <p>Loads the conversation entity and all associated messages, mapping
     * them to DTOs with role labels, tool names, and timestamps. Throws if
     * the conversation does not exist.</p>
     *
     * @param chatId the public conversation identifier
     * @return the full conversation detail DTO
     */
    AiChatLogDetailDto getConversationDetail(String chatId);

    /**
     * Deletes a single chat message by its database ID.
     *
     * <p>Returns false if no message exists with the given ID. Does not
     * cascade to parent conversations or affect other messages.</p>
     *
     * @param messageId the message's database ID
     * @return true if the message was found and deleted, false otherwise
     */
    boolean deleteMessage(Long messageId);

    /**
     * Deletes an entire conversation and all its messages.
     *
     * <p>Performs a cascading delete by removing all child messages before
     * deleting the conversation entity. Throws if the conversation does
     * not exist.</p>
     *
     * @param chatId the public conversation identifier
     */
    void deleteConversation(String chatId);

    /**
     * Marks a conversation as resolved.
     *
     * <p>Updates the conversation status to RESOLVED, sets the resolution
     * timestamp, and appends a SYSTEM message to the conversation history.
     * Throws if the conversation does not exist.</p>
     *
     * @param chatId the public conversation identifier
     */
    void resolveConversation(String chatId);
}
