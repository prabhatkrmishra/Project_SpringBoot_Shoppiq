package com.pkmprojects.shoppiq.entity;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.enums.ContactMessageStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a message submitted via the contact form.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Entity
@Table(name = "contact_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class ContactMessage extends AuditableEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContactMessageStatus status = ContactMessageStatus.PENDING;
}
