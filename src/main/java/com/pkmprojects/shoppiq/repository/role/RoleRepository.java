package com.pkmprojects.shoppiq.repository.role;

import com.pkmprojects.shoppiq.entity.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Persistence operations for the {@link Role} aggregate.
 *
 * <p>Provides methods to query roles by name for authentication and authorization workflows.
 * The repository supports role lookups for permission checking and role assignment during
 * user registration and admin management.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Finds a role by its name.
     *
     * @param roleName the role name (e.g., {@code ROLE_CUSTOMER})
     * @return optional role
     */
    Optional<Role> findByRoleName(String roleName);
}
