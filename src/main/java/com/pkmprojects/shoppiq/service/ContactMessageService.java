package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.request.ContactMessageRequest;
import com.pkmprojects.shoppiq.dto.response.ContactMessageResponse;

import java.util.List;

/**
 * Service for contact message operations.
 *
 * @author PrabhatKrMishra
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
     * Returns all contact messages ordered by creation date descending.
     *
     * @return list of all messages
     */
    List<ContactMessageResponse> getAllMessages();

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
