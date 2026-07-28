package com.pkmprojects.shoppiq.service.contact;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.contact.ContactMessageRequest;
import com.pkmprojects.shoppiq.dto.contact.ContactMessageResponse;

/**
 * <strong>Spring Boot Concept:</strong> Service for contact message operations.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * Defines the <strong>Service layer</strong> contract for managing contact form submissions.
 * Architecture: {@code ContactMessageController → ContactMessageService → ContactMessageRepository}.
 * </p>
 *
 * <h2>Business Logic Responsibilities</h2>
 * <ul>
 *   <li>Accept contact form submissions from visitors (public endpoint).</li>
 *   <li>Admin retrieval of all messages with pagination (sorted newest-first).</li>
 *   <li>View a single message (auto-marks as READ).</li>
 *   <li>Toggle read/unread status for tracking.</li>
 *   <li>Count unread messages for admin badge notifications.</li>
 *   <li>Delete messages.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface ContactMessageService {

    /**
     * Creates a new contact message.
     *
     * @param request the contact message details
     * @return the created contact message response
     */
    ContactMessageResponse create(ContactMessageRequest request);

    /**
     * Returns all contact messages ordered by creation date descending, paginated.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return paginated message responses
     */
    PageResponse<ContactMessageResponse> getAllMessages(int page, int size);

    /**
     * Returns a single message by ID and marks it as READ.
     *
     * @param id the message ID
     * @return the message response
     */
    ContactMessageResponse getMessageById(Long id);

    /**
     * Deletes a contact message.
     *
     * @param id the message ID
     */
    void deleteMessage(Long id);

    /**
     * Marks a message as READ.
     *
     * @param id the message ID
     * @return the updated message response
     */
    ContactMessageResponse markAsRead(Long id);

    /**
     * Marks a message as PENDING (unread).
     *
     * @param id the message ID
     * @return the updated message response
     */
    ContactMessageResponse markAsUnread(Long id);

    /**
     * Returns the count of unread (PENDING) messages.
     *
     * @return number of unread messages
     */
    long countUnreadMessages();
}
