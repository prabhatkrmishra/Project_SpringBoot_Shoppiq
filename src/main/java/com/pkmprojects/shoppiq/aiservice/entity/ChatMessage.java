package com.pkmprojects.shoppiq.aiservice.entity;

import com.pkmprojects.shoppiq.aiservice.enums.ChatMessageRole;
import com.pkmprojects.shoppiq.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity representing a single message within an AI chat conversation.
 *
 * <p>This entity stores individual messages in sequential order, capturing
 * the full conversational history for each AI chat. Messages are rendered
 * in chronological order with role-based visual treatment in the UI. Each
 * message is linked to its parent {@link ChatConversation} via a
 * many-to-one relationship.</p>
 *
 * <p>The entity supports four message roles: USER (human input), ASSISTANT
 * (AI-generated response), SYSTEM (internal events like conversation
 * resolution), and TOOL (output from AI tool invocations). Tool messages
 * include the {@code toolName} field identifying which tool produced the
 * output, enabling the UI to display appropriate tool attribution.</p>
 *
 * @author prabhatkrmishra
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
     *
     * <p>Establishes the foreign key relationship to the owning
     * {@link ChatConversation}. The fetch type is LAZY to avoid
     * loading the full conversation graph when querying individual
     * messages.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    /**
     * The role of this message within the conversation.
     *
     * <p>Determines how the message is rendered in the UI and how it
     * contributes to the AI model's context window. Stored as a string
     * enumeration in the database.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ChatMessageRole role;

    /**
     * The textual content of the message.
     *
     * <p>Stores the full text of user messages, AI responses, system
     * notifications, and tool outputs. Uses TEXT column type to
     * accommodate lengthy AI-generated responses.</p>
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Name of the tool that produced this message (e.g., {@code search_products}).
     *
     * <p>This field is {@code null} for USER, ASSISTANT, and SYSTEM messages.
     * It is populated only for TOOL messages to identify which AI tool
     * invocation generated the output, enabling the UI to display tool
     * attribution and allowing admins to audit tool usage patterns.</p>
     */
    @Column(name = "tool_name", length = 128)
    private String toolName;

    /**
     * Number of tokens consumed by this message.
     *
     * <p>Used for usage tracking and cost monitoring of AI API calls.
     * This field is optional and may be {@code null} for messages where
     * token counting was not performed or is not applicable.</p>
     */
    @Column(name = "tokens_used")
    private Integer tokensUsed;
}
