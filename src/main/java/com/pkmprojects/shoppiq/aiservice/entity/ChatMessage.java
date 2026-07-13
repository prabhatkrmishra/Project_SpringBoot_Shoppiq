package com.pkmprojects.shoppiq.aiservice.entity;

import com.pkmprojects.shoppiq.aiservice.enums.ChatMessageRole;
import com.pkmprojects.shoppiq.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity representing a single message within an AI chat conversation.
 *
 * <p>
 * Messages are stored sequentially and rendered in chronological order. Each
 * message has a {@link ChatMessageRole} that determines its visual treatment
 * in the UI (e.g., user messages right-aligned, assistant messages left-aligned).
 *
 * <h2>Relationships</h2>
 * <ul>
 *   <li>{@link ChatConversation} — the parent conversation, via {@code conversation_id}</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChatMessage extends AuditableEntity {

    /**
     * The parent conversation this message belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    /**
     * The role of this message within the conversation.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ChatMessageRole role;

    /**
     * The textual content of the message.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Name of the tool that produced this message (e.g., {@code search_products}). {@code null} for non-tool messages.
     */
    @Column(name = "tool_name", length = 128)
    private String toolName;

    /**
     * Number of tokens consumed by this message (used for usage tracking).
     */
    @Column(name = "tokens_used")
    private Integer tokensUsed;
}
