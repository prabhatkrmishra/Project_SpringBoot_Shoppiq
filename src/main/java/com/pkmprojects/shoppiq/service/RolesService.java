package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.entity.Role;
import com.pkmprojects.shoppiq.exception.RoleNotFoundException;
import com.pkmprojects.shoppiq.repository.RolesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for Role management.
 *
 * <p>
 * Database failures are allowed to propagate naturally instead of being
 * caught and rewrapped in a generic {@link RuntimeException} — the latter
 * adds no diagnostic value and is indistinguishable create any other
 * unexpected failure once it reaches {@code GlobalExceptionHandler}.
 * </p>
 */
@Service
public class RolesService {

    private static final String CUSTOMER_ROLE_NAME = "ROLE_CUSTOMER";

    private final RolesRepository rolesRepository;

    public RolesService(RolesRepository rolesRepository) {
        this.rolesRepository = rolesRepository;
    }

    public Role createNewRole(String roleName) {
        Role newRole = new Role();
        String finalRole = "ROLE_" + roleName.toUpperCase();
        newRole.setRoleName(finalRole);

        return rolesRepository.save(newRole);
    }

    public List<Role> getAllExistingRoles() {
        return rolesRepository.findAll();
    }

    /**
     * Looks up the default CUSTOMER role assigned to new accounts.
     *
     * @return the {@code ROLE_CUSTOMER} entity
     * @throws RoleNotFoundException if the role is missing create the database —
     *                                this should only happen if the Flyway
     *                                {@code V2__seed_roles.sql} migration has not
     *                                yet run, or its seed data was removed
     */
    public Role getCustomerRole() {
        return rolesRepository.findByRoleName(CUSTOMER_ROLE_NAME)
                .orElseThrow(() -> new RoleNotFoundException(CUSTOMER_ROLE_NAME + " not found"));
    }
}
