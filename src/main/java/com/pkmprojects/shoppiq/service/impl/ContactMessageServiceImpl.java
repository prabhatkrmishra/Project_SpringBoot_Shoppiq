package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.request.ContactMessageRequest;
import com.pkmprojects.shoppiq.dto.response.ContactMessageResponse;
import com.pkmprojects.shoppiq.entity.ContactMessage;
import com.pkmprojects.shoppiq.enums.ContactMessageStatus;
import com.pkmprojects.shoppiq.exception.ContactMessageNotFoundException;
import com.pkmprojects.shoppiq.repository.ContactMessageRepository;
import com.pkmprojects.shoppiq.service.ContactMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    @Transactional(readOnly = true)
    public List<ContactMessageResponse> getAllMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ContactMessageResponse::fromEntity)
                .toList();
    }

    @Override
    public ContactMessageResponse getMessageById(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> ContactMessageNotFoundException.id(id));

        if (message.getStatus() != ContactMessageStatus.READ) {
            message.setStatus(ContactMessageStatus.READ);
            message = contactMessageRepository.save(message);
        }

        return ContactMessageResponse.fromEntity(message);
    }

    @Override
    public void deleteMessage(Long id) {
        if (!contactMessageRepository.existsById(id)) {
            throw ContactMessageNotFoundException.id(id);
        }
        contactMessageRepository.deleteById(id);
    }

    @Override
    public ContactMessageResponse markAsRead(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> ContactMessageNotFoundException.id(id));

        message.setStatus(ContactMessageStatus.READ);
        message = contactMessageRepository.save(message);
        return ContactMessageResponse.fromEntity(message);
    }

    @Override
    public ContactMessageResponse markAsUnread(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> ContactMessageNotFoundException.id(id));

        message.setStatus(ContactMessageStatus.PENDING);
        message = contactMessageRepository.save(message);
        return ContactMessageResponse.fromEntity(message);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadMessages() {
        return contactMessageRepository.countByStatus(ContactMessageStatus.PENDING);
    }
}
