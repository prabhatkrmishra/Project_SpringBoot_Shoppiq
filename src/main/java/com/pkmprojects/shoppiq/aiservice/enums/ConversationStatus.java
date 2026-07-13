package com.pkmprojects.shoppiq.aiservice.enums;

/**
 * Represents the lifecycle status of an AI chat conversation.
 *
 * <p>
 * Conversations transition from {@link #ACTIVE} to {@link #RESOLVED} when the
 * user explicitly marks the conversation as complete, or when the system
 * auto-resolves based on closing-phrase detection.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public enum ConversationStatus {

    /**
     * Conversation is ongoing and can accept new messages.
     */
    ACTIVE,

    /**
     * Conversation has been closed; no further messages are permitted.
     */
    RESOLVED
}
