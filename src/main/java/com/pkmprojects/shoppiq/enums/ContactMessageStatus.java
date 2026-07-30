package com.pkmprojects.shoppiq.enums;

/**
 * Lifecycle status of a contact message submitted via the contact form.
 *
 * <p>This enum tracks the processing state of customer support messages.
 * Messages progress through PENDING to READ to REPLIED as admins view
 * and respond to them. The status helps administrators prioritize
 * unresolved messages and track response times.</p>
 *
 * <p>PENDING messages are displayed prominently in the admin dashboard
 * to indicate unread messages. READ messages have been viewed but not
 * yet responded to. REPLIED messages are fully processed and archived.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum ContactMessageStatus {
    /**
     * Message has been submitted but not yet viewed by an admin.
     *
     * <p>This is the initial state for all contact form submissions.
     * Admins should prioritize PENDING messages to ensure timely
     * customer support.</p>
     */
    PENDING,
    /**
     * Message has been viewed by an admin but not yet responded to.
     *
     * <p>The admin has acknowledged the message but has not yet
     * composed or sent a response. This status helps track messages
     * that are in progress.</p>
     */
    READ,
    /**
     * Admin has responded to the message.
     *
     * <p>This is the terminal state for contact messages. The
     * customer has received a response and the conversation is
     * considered closed.</p>
     */
    REPLIED
}
