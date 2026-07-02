package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.request.ContactMessageRequest;
import com.pkmprojects.shoppiq.dto.response.ContactMessageResponse;
import com.pkmprojects.shoppiq.entity.ContactMessage;
import com.pkmprojects.shoppiq.repository.ContactMessageRepository;
import com.pkmprojects.shoppiq.service.ContactMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link ContactMessageService}.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactMessageServiceImpl(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @Override
    public ContactMessageResponse create(ContactMessageRequest request) {
        ContactMessage message = ContactMessage.builder()
                .name(request.name())
                .email(request.email())
                .subject(request.subject())
                .message(request.message())
                .build();

        message = contactMessageRepository.save(message);
        return ContactMessageResponse.fromEntity(message);
    }
}
