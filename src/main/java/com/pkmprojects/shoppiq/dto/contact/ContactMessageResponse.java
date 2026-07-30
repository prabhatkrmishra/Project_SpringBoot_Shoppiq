package com.pkmprojects.shoppiq.dto.contact;

import com.pkmprojects.shoppiq.entity.contact.ContactMessage;
import com.pkmprojects.shoppiq.enums.ContactMessageStatus;

import java.time.Instant;

/**
 * Response DTO for a submitted contact message.
 *
 * <p>This record is returned when a user submits a contact form
 * (confirming receipt) and when administrators view submitted messages
 * in the support dashboard. It includes a {@code status} field that
 * tracks the message's position in the support workflow (OPEN,
 * IN_PROGRESS, RESOLVED), enabling administrators to manage and
 * resolve customer inquiries.</p>
 *
 * <p>The static {@link #fromEntity(ContactMessage)} factory method
 * provides a centralized entity-to-DTO mapping. The response includes
 * all original submission data along with the server-assigned identifier
 * and timestamp, allowing the frontend to display confirmation details
 * and the admin UI to show message history.</p>
 *
 * @param id        unique identifier of the contact message, auto-generated
 *                  by the database
 * @param name      sender's display name as submitted in the contact form
 * @param email     sender's email address for reply correspondence
 * @param subject   message subject line for categorization
 * @param message   message body text containing the customer's inquiry
 * @param status    current support workflow status (OPEN, IN_PROGRESS,
 *                  RESOLVED) tracking the message lifecycle
 * @param createdAt timestamp when the message was first submitted
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ContactMessageResponse(
        /**
         * Unique identifier of the contact message.
         */
        Long id,

        /**
         * Sender's name.
         */
        String name,

        /**
         * Sender's email address.
         */
        String email,

        /**
         * Message subject.
         */
        String subject,

        /**
         * Message body.
         */
        String message,

        /**
         * Current status (OPEN, IN_PROGRESS, RESOLVED).
         */
        ContactMessageStatus status,

        /**
         * Message creation timestamp.
         */
        Instant createdAt
) {
    /**
     * Creates a response DTO from the given entity.
     *
     * @param entity the contact message entity
     * @return populated response DTO
     */
    public static ContactMessageResponse fromEntity(ContactMessage entity) {
        return new ContactMessageResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getSubject(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
