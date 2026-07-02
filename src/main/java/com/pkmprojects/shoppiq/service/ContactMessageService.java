package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.request.ContactMessageRequest;
import com.pkmprojects.shoppiq.dto.response.ContactMessageResponse;

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
}
