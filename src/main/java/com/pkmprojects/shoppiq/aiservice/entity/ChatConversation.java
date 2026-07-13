package com.pkmprojects.shoppiq.aiservice.entity;

import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;
import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * JPA entity representing an AI chat conversation.
 *
 * <p>
 * Each conversation is identified by a public-facing {@code chatId} (e.g.,
 * {@code CHAT-2026-07-A3F2}) and is associated with either an authenticated
 * {@link User} or a guest session identified by a random UUID.
 *
 * <h2>Relationships</h2>
 * <ul>
 *   <li>{@link #user} — the authenticated user who owns this conversation (nullable for guests)</li>
 *   <li>{@link com.pkmprojects.shoppiq.aiservice.entity.ChatMessage} — child messages via {@code conversation_id}</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li>Guest conversations are identified by {@code guestSession} rather than a user FK</li>
 *   <li>The {@code title} is auto-generated from the user's first message</li>
 *   <li>{@code resolvedAt} is set when the conversation transitions to {@link ConversationStatus#RESOLVED}</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
     * The authenticated user who owns this conversation. {@code null} for guest sessions.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Public-facing conversation identifier (e.g., {@code CHAT-2026-07-A3F2}). Unique across all conversations.
     */
    @Column(name = "chat_id", unique = true, nullable = false, length = 20)
    private String chatId;

    /**
     * Auto-generated title derived from the user's first message.
     */
    @Column(nullable = false)
    private String title = "New Conversation";

    /**
     * Current lifecycle status of this conversation.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ConversationStatus status = ConversationStatus.ACTIVE;

    /**
     * Timestamp when the conversation was marked as resolved. {@code null} if still active.
     */
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    /**
     * Random UUID identifying a guest session. {@code null} for authenticated users.
     */
    @Column(name = "guest_session", length = 64)
    private String guestSession;

    /**
     * IP address of the guest user at the time of conversation creation.
     */
    @Column(name = "guest_ip", length = 45)
    private String guestIp;
}
