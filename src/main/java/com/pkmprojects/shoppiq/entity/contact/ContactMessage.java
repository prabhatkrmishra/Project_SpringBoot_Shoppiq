package com.pkmprojects.shoppiq.entity.contact;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.enums.ContactMessageStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * <strong>Spring Boot Concept:</strong> Represents a message submitted via the contact form.
 *
 * <p>Stores customer inquiries submitted through the public contact page.
 * Admins can view, read, and reply to messages, with status tracked
 * via {@link ContactMessageStatus}.</p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>{@code @Enumerated(EnumType.STRING)}</strong> — Stores the
 *         enum value as a readable string in the database, making schema
 *         inspection and debugging easier than storing ordinals.</li>
 *     <li><strong>{@code @Builder.Default}</strong> — The default
 *         {@code PENDING} status means new messages start in a known state
 *         without requiring the caller to set it explicitly.</li>
 *     <li><strong>Extends {@link AuditableEntity}</strong> — Inherits automatic
 *         ID, versioning, and audit timestamps ({@code createdAt},
 *         {@code updatedAt}).</li>
 *     <li><strong>Simple entity with no relationships</strong> —
 *         Demonstrates that entities do not need JPA associations;
 *         {@code ContactMessage} stores all data as scalar columns with no
 *         FK references to other tables.</li>
 *     <li><strong>{@code @EqualsAndHashCode(of = "id", callSuper = false)}</strong>
 *         — Uses only the {@code id} field for equality, explicitly excluding
 *         the superclass to avoid issues with {@code AuditableEntity} fields.</li>
 * </ul>
 *
 * @author prabhatkrmishra
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
