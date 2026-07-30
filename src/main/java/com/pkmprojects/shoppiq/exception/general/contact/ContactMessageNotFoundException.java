package com.pkmprojects.shoppiq.exception.general.contact;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a requested contact message cannot be found.
 *
 * <p>This exception is thrown by contact service methods when a database
 * lookup for a contact message fails. It uses the
 * {@link ErrorCode#CONTACT_MESSAGE_NOT_FOUND} code and HTTP 404 Not
 * Found status. The message may have been deleted by another
 * administrator.</p>
 *
 * <p>The detail message includes the message identifier (e.g.,
 * "Contact message with id '42' was not found.") to help the client
 * understand which message was invalid. The administrator should verify
 * the message ID and retry the operation.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#CONTACT_MESSAGE_NOT_FOUND
 * @since 1.0.0
 */
public final class ContactMessageNotFoundException extends ResourceNotFoundException {

    private ContactMessageNotFoundException(String detail) {
        super(ErrorCode.CONTACT_MESSAGE_NOT_FOUND, detail);
    }

    /**
     * Creates an exception indicating that no contact message exists with the
     * supplied identifier.
     *
     * @param id contact message identifier
     * @return contact message not found exception
     */
    public static ContactMessageNotFoundException id(Long id) {
        return new ContactMessageNotFoundException(
                "Contact message with id '%d' was not found.".formatted(id)
        );
    }
}
