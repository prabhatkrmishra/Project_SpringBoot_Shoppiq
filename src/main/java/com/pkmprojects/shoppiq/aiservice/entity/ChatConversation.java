package com.pkmprojects.shoppiq.aiservice.entity;

import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;
import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * JPA entity representing an AI chat conversation.
 *
 * <p>This entity is the root aggregate for the AI chat domain, encapsulating
 * the conversation's lifecycle state, ownership, and metadata. Each
 * conversation is identified by a public-facing {@code chatId} in the
 * format {@code CHAT-yyyy-MM-XXXX} and is associated with either an
 * authenticated user (via the {@code user} relationship) or a guest
 * session (via the {@code guestSession} UUID and {@code guestIp} fields).</p>
 *
 * <p>The entity extends {@link AuditableEntity} to inherit automatic
 * {@code createdAt} and {@code updatedAt} timestamp management. The
 * {@code status} field tracks the conversation lifecycle from
 * {@link ConversationStatus#ACTIVE} through to
 * {@link ConversationStatus#RESOLVED}, with the {@code resolvedAt}
 * timestamp recording when resolution occurred.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Entity
@Table(name = "chat_conversations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChatConversation extends AuditableEntity {

    /**
     * The authenticated user who owns this conversation.
     *
     * <p>This field is {@code null} for guest sessions where no user account
     * is associated. For authenticated conversations, it establishes the
     * ownership relationship used for access control and conversation
     * listing.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Public-facing conversation identifier (e.g., {@code CHAT-2026-07-A3F2}).
     *
     * <p>This unique identifier is exposed to the frontend and used as the
     * primary reference for all conversation operations. It is generated
     * with a year-month prefix for human readability and a random
     * alphanumeric suffix for uniqueness.</p>
     */
    @Column(name = "chat_id", unique = true, nullable = false, length = 20)
    private String chatId;

    /**
     * Auto-generated title derived from the user's first message.
     *
     * <p>Initialized to "New Conversation" and updated with the first 50
     * characters of the user's initial message when the first exchange
     * occurs.</p>
     */
    @Column(nullable = false)
    private String title = "New Conversation";

    /**
     * Current lifecycle status of this conversation.
     *
     * <p>Transitions from {@link ConversationStatus#ACTIVE} to
     * {@link ConversationStatus#RESOLVED} when the user marks the
     * conversation complete, the system auto-resolves due to inactivity,
     * or an admin manually resolves it.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ConversationStatus status = ConversationStatus.ACTIVE;

    /**
     * Timestamp when the conversation was marked as resolved.
     *
     * <p>This field is {@code null} while the conversation is active and
     * is populated when the conversation transitions to the RESOLVED
     * status. Used for admin reporting and inactivity-based auto-resolution.</p>
     */
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    /**
     * Random UUID identifying a guest session.
     *
     * <p>This field is {@code null} for authenticated users. For guest
     * conversations, it stores the session UUID from the
     * {@code GUEST_SESSION} cookie, enabling conversation tracking
     * without user authentication.</p>
     */
    @Column(name = "guest_session", length = 64)
    private String guestSession;

    /**
     * IP address of the guest user at the time of conversation creation.
     *
     * <p>Stored for rate limiting and abuse prevention for unauthenticated
     * guest sessions. Supports both IPv4 and IPv6 formats (max 45 chars).</p>
     */
    @Column(name = "guest_ip", length = 45)
    private String guestIp;
}
