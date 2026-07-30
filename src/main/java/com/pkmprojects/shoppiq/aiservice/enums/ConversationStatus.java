package com.pkmprojects.shoppiq.aiservice.enums;

/**
 * Lifecycle status of an AI chat conversation.
 *
 * <p>This enumeration defines the valid states for an AI chat conversation's
 * lifecycle. Conversations transition from {@link #ACTIVE} to
 * {@link #RESOLVED} when the user marks them complete, the system auto-resolves
 * them due to inactivity, or an admin manually resolves them. Once resolved,
 * a conversation cannot accept new messages and is considered closed.</p>
 *
 * <p>The status field is persisted as a string in the database and is used
 * for filtering, auto-resolution queries, and admin dashboard status
 * indicators.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum ConversationStatus {

    /**
     * Conversation is ongoing and can accept new messages.
     *
     * <p>This is the initial status for all new conversations. The AI model
     * processes messages and the conversation context is maintained in the
     * chat memory window.</p>
     */
    ACTIVE,

    /**
     * Conversation has been closed; no further messages are permitted.
     *
     * <p>Once a conversation reaches this status, any attempt to send a new
     * message results in an {@code AiAssistantException} with a 410 Gone
     * response. The conversation remains in the database for admin auditing
     * and historical review.</p>
     */
    RESOLVED
}
