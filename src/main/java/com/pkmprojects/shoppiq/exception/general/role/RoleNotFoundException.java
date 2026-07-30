package com.pkmprojects.shoppiq.exception.general.role;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a requested role cannot be found.
 *
 * <p>This exception is thrown by role service methods when a database
 * lookup for a role fails. It uses the
 * {@link ErrorCode#ROLE_NOT_FOUND} code and HTTP 404 Not Found status.
 * Roles are seeded at application startup and should always be present
 * under normal circumstances.</p>
 *
 * <p>The detail message includes the role name (e.g., "Role 'ADMIN' was
 * not found.") to help the client understand which role was invalid. The
 * client should verify the role name and retry the request.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#ROLE_NOT_FOUND
 * @since 1.0.0
 */
public final class RoleNotFoundException extends ResourceNotFoundException {

    private RoleNotFoundException(String detail) {
        super(ErrorCode.ROLE_NOT_FOUND, detail);
    }

    /**
     * Creates an exception for a role not found by its name.
     *
     * @param roleName the role name
     * @return a new exception instance
     */
    public static RoleNotFoundException forName(String roleName) {
        return new RoleNotFoundException(
                "Role '%s' was not found.".formatted(roleName));
    }
}
