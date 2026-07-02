package com.pkmprojects.shoppiq.dto.response;

import com.pkmprojects.shoppiq.entity.ContactMessage;
import com.pkmprojects.shoppiq.enums.ContactMessageStatus;

import java.time.Instant;

/**
 * Response DTO for a contact message.
 *
 * @author PrabhatKrMishra
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
