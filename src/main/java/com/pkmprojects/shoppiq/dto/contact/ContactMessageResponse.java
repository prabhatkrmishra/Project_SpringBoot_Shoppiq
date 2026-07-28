package com.pkmprojects.shoppiq.dto.contact;

import com.pkmprojects.shoppiq.entity.contact.ContactMessage;
import com.pkmprojects.shoppiq.enums.ContactMessageStatus;

import java.time.Instant;

/**
 * Response DTO for a contact message.
 *
 * <p>Returned when a user submits a contact form (confirming receipt) and
 * when admins view submitted messages. Includes a {@code status} field
 * ({@link com.pkmprojects.shoppiq.enums.ContactMessageStatus}) for
 * tracking the message's state (OPEN, IN_PROGRESS, RESOLVED).</p>
 *
 * <p><b>Mapping pattern:</b> The {@link #fromEntity(com.pkmprojects.shoppiq.entity.contact.ContactMessage) fromEntity()}
 * method maps the JPA entity to this DTO — a simple, centralized conversion
 * that keeps the entity layer internal.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record ContactMessageResponse(
        Long id,
        String name,
        String email,
        String subject,
        String message,
        ContactMessageStatus status,
        Instant createdAt
) {
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
