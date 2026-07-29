package com.pkmprojects.shoppiq.service.contact;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.contact.ContactMessageRequest;
import com.pkmprojects.shoppiq.dto.contact.ContactMessageResponse;
import com.pkmprojects.shoppiq.entity.contact.ContactMessage;
import com.pkmprojects.shoppiq.enums.ContactMessageStatus;
import com.pkmprojects.shoppiq.exception.general.contact.ContactMessageNotFoundException;
import com.pkmprojects.shoppiq.repository.contact.ContactMessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * <strong>Spring Boot Concept:</strong> Implementation of {@link ContactMessageService}
 * containing business logic for contact message management.
 *
 * <p>Handles public contact form submissions, admin paginated retrieval, auto-marking
 * messages as READ on view, manual read/unread toggling, and unread message counting.
 * Used by {@code ContactMessageController}.</p>
 *
 * <p>Why this design:
 * <ul>
 *   <li><strong>@Service</strong> — Spring stereotype for service-layer beans, auto-detected via component scanning.</li>
 *   <li><strong>@Transactional</strong> — Status changes are atomic; reads use {@code readOnly = true}.</li>
 *   <li><strong>Constructor injection</strong> — final fields for immutability and testability.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see ContactMessageService
 * @since 1.0.0
 */
@Service
@Transactional
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactMessageServiceImpl(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    /**
     * Creates a new contact message from a public form submission.
     *
     * @param request contact message payload
     * @return created contact message response
     */
    @Override
    public ContactMessageResponse create(ContactMessageRequest request) {
        Objects.requireNonNull(request, "Contact message request must not be null.");
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Name must not be blank.");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email must not be blank.");
        }
        if (request.message() == null || request.message().isBlank()) {
            throw new IllegalArgumentException("Message must not be blank.");
        }

        ContactMessage message = ContactMessage.builder()
                .name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .subject(request.subject() != null ? request.subject().trim() : null)
                .message(request.message().trim())
                .build();

        message = contactMessageRepository.save(message);
        return ContactMessageResponse.fromEntity(message);
    }

    /**
     * Retrieves a paginated list of all contact messages, newest first.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated contact message responses
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ContactMessageResponse> getAllMessages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var messagePage = contactMessageRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PageResponse.of(messagePage, ContactMessageResponse::fromEntity);
    }

    /**
     * Retrieves a contact message by ID, auto-marking it as READ on first view.
     *
     * @param id contact message ID
     * @return contact message response
     * @throws ContactMessageNotFoundException if the message does not exist
     */
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

    /**
     * Deletes a contact message by ID.
     *
     * @param id contact message ID
     * @throws ContactMessageNotFoundException if the message does not exist
     */
    @Override
    public void deleteMessage(Long id) {
        if (!contactMessageRepository.existsById(id)) {
            throw ContactMessageNotFoundException.id(id);
        }
        contactMessageRepository.deleteById(id);
    }

    /**
     * Marks a contact message as READ.
     *
     * @param id contact message ID
     * @return updated contact message response
     * @throws ContactMessageNotFoundException if the message does not exist
     */
    @Override
    public ContactMessageResponse markAsRead(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> ContactMessageNotFoundException.id(id));

        message.setStatus(ContactMessageStatus.READ);
        message = contactMessageRepository.save(message);
        return ContactMessageResponse.fromEntity(message);
    }

    /**
     * Marks a contact message as UNREAD (PENDING).
     *
     * @param id contact message ID
     * @return updated contact message response
     * @throws ContactMessageNotFoundException if the message does not exist
     */
    @Override
    public ContactMessageResponse markAsUnread(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> ContactMessageNotFoundException.id(id));

        message.setStatus(ContactMessageStatus.PENDING);
        message = contactMessageRepository.save(message);
        return ContactMessageResponse.fromEntity(message);
    }

    /**
     * Returns the count of unread (PENDING) contact messages.
     *
     * @return count of unread messages
     */
    @Override
    @Transactional(readOnly = true)
    public long countUnreadMessages() {
        return contactMessageRepository.countByStatus(ContactMessageStatus.PENDING);
    }
}
