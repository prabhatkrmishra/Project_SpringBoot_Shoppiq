package com.pkmprojects.shoppiq.entity.contact;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.enums.ContactMessageStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a message submitted via the public contact form.
 *
 * <p>Stores customer inquiries submitted through the contact page, including
 * the sender's identity, subject, and full message body. Admins can view,
 * read, and reply to messages through the admin dashboard, with the
 * current handling status tracked via {@link ContactMessageStatus}.</p>
 *
 * <p>Messages follow a moderation workflow: new submissions start as
 * {@code PENDING}, transition to {@code READ} when an admin views them,
 * and optionally to {@code REPLIED} after a response is sent. This entity
 * is not tied to a user account, allowing both registered users and
 * anonymous visitors to submit inquiries.</p>
 *
 * @author prabhatkrmishra
 * @see ContactMessageStatus
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

    /**
     * Sender's full name as provided in the contact form.
     *
     * <p>Required field with a maximum length of 100 characters. Used
     * to identify the sender in the admin dashboard and in reply
     * communications. This field is not validated against a user
     * account since anonymous submissions are permitted.</p>
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Sender's email address for reply communication and inquiry
     * tracking.
     *
     * <p>Required field with a maximum length of 255 characters. Used
     * as the reply-to address when admin staff respond to the inquiry.
     * Not validated for uniqueness since multiple inquiries from the
     * same email are expected.</p>
     */
    @Column(nullable = false, length = 255)
    private String email;

    /**
     * Subject line summarizing the purpose or topic of the inquiry.
     *
     * <p>Required field with a maximum length of 200 characters. Displayed
     * in the admin inbox as the primary identifier for each message.
     * Helps administrators quickly triage and route inquiries to the
     * appropriate team.</p>
     */
    @Column(nullable = false, length = 200)
    private String subject;

    /**
     * Full message body submitted by the customer describing their
     * inquiry, issue, or feedback.
     *
     * <p>Required field stored as a TEXT column to accommodate lengthy
     * messages. Contains the complete customer communication and is
     * displayed in the admin detail view. No length constraint is
     * enforced at the database level.</p>
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Current moderation and handling status of this contact message.
     *
     * <p>Stored as a string enum with a maximum length of 20 characters.
     * Defaults to {@link ContactMessageStatus#PENDING} for new
     * submissions. Transitions through READ and REPLIED as
     * administrators process the inquiry. The status drives the
     * admin inbox filtering and notification workflows.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContactMessageStatus status = ContactMessageStatus.PENDING;
}
