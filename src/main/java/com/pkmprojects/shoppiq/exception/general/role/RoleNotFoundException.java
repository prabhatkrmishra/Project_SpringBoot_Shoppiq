package com.pkmprojects.shoppiq.exception.general.role;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a requested
 * role cannot be found.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) for missing roles. Typically thrown when seed roles
 * ({@code ROLE_CUSTOMER}, {@code ROLE_SELLER}) are absent from the
 * database.</p>
 *
 * @author prabhatkrmishra
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
