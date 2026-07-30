package com.pkmprojects.shoppiq.service.role;

import com.pkmprojects.shoppiq.entity.role.Role;
import com.pkmprojects.shoppiq.exception.general.role.RoleNotFoundException;
import com.pkmprojects.shoppiq.repository.role.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for role management and default role lookups.
 *
 * <p>Provides customer and seller role retrieval, relying on Flyway seed data.
 * Database failures propagate naturally for diagnostic clarity.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Service
public class RoleService {

    private static final String CUSTOMER_ROLE_NAME = "ROLE_CUSTOMER";
    private static final String SELLER_ROLE_NAME = "ROLE_SELLER";

    private final RoleRepository rolesRepository;

    public RoleService(RoleRepository rolesRepository) {
        this.rolesRepository = rolesRepository;
    }

    /**
     * Creates a new role with the given name, automatically prefixing with {@code ROLE_}.
     *
     * @param roleName the role name (without the ROLE_ prefix)
     * @return the created role entity
     */
    public Role createNewRole(String roleName) {
        Role newRole = new Role();
        String finalRole = "ROLE_" + roleName.toUpperCase();
        newRole.setRoleName(finalRole);

        return rolesRepository.save(newRole);
    }

    /**
     * Returns all existing roles in the system.
     *
     * @return list of all role entities
     */
    public List<Role> getAllExistingRoles() {
        return rolesRepository.findAll();
    }

    /**
     * Looks up the default CUSTOMER role assigned to new accounts.
     *
     * @return the {@code ROLE_CUSTOMER} entity
     * @throws RoleNotFoundException if the role is missing from the database —
     *                               this should only happen if the Flyway
     *                               {@code V2__seed_roles.sql} migration has not
     *                               yet run, or its seed data was removed
     */
    public Role getCustomerRole() {
        return rolesRepository.findByRoleName(CUSTOMER_ROLE_NAME)
                .orElseThrow(() -> RoleNotFoundException.forName(CUSTOMER_ROLE_NAME));
    }

    /**
     * Looks up the SELLER role.
     *
     * @return the {@code ROLE_SELLER} entity
     * @throws RoleNotFoundException if the role is missing
     */
    public Role getSellerRole() {
        return rolesRepository.findByRoleName(SELLER_ROLE_NAME)
                .orElseThrow(() -> RoleNotFoundException.forName(SELLER_ROLE_NAME));
    }
}
