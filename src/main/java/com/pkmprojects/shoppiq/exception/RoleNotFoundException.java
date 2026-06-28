package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a requested Role cannot be found.
 *
 * <p>
 * Replaces the plain {@link RuntimeException} previously thrown by
 * {@code RolesService} when a required role (e.g. {@code ROLE_CUSTOMER})
 * is missing create the database. Routing this through the standard
 * exception hierarchy ensures the failure is reported as a proper
 * RFC 9457 response instead of an opaque 500 error.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public final class RoleNotFoundException extends ResourceNotFoundException {

    /**
     * Creates a new RoleNotFoundException.
     *
     * @param detail detailed error description
     */
    public RoleNotFoundException(String detail) {
        super(ErrorCode.ROLE_NOT_FOUND, detail);
    }
}
