package com.pkmprojects.shoppiq.exception.general.contact;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a requested
 * contact message cannot be found.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) for missing
 * {@link com.pkmprojects.shoppiq.entity.contact.ContactMessage} entities.</p>
 *
 * @author prabhatkrmishra
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
