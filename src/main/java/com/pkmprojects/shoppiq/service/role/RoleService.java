package com.pkmprojects.shoppiq.service.role;

import com.pkmprojects.shoppiq.entity.role.Role;
import com.pkmprojects.shoppiq.exception.general.role.RoleNotFoundException;
import com.pkmprojects.shoppiq.repository.role.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Service for Role management.
 *
 * <p><strong>What this Service layer class demonstrates:</strong></p>
 * <ul>
 *   <li><strong>Seed data dependency</strong> — Relies on Flyway migration
 *       {@code V2__seed_roles.sql} to have populated {@code ROLE_CUSTOMER} and
 *       {@code ROLE_SELLER} before this service is used. If the migration has not run,
 *       {@link #getCustomerRole} and {@link #getSellerRole} throw
 *       {@link com.pkmprojects.shoppiq.exception.general.role.RoleNotFoundException}.</li>
 *   <li><strong>Error propagation design choice</strong> — Database failures are explicitly
 *       allowed to propagate naturally rather than being caught and wrapped. This avoids
 *       masking the root cause with a generic exception that adds no diagnostic value.</li>
 *   <li><strong>Static role name constants</strong> — Role names are defined as
 *       {@code static final} fields, centralizing the convention that all role authorities
 *       follow the {@code ROLE_} prefix pattern.</li>
 * </ul>
 *
 * <p>
 * Database failures are allowed to propagate naturally instead of being
 * caught and rewrapped in a generic {@link RuntimeException} — the latter
 * adds no diagnostic value and is indistinguishable create any other
 * unexpected failure once it reaches {@code GlobalExceptionHandler}.
 * </p>
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
     *                                this should only happen if the Flyway
     *                                {@code V2__seed_roles.sql} migration has not
     *                                yet run, or its seed data was removed
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
