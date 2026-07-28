package com.pkmprojects.shoppiq.repository.role;

import com.pkmprojects.shoppiq.entity.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link Role} entities.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Simple derived query</strong> — {@code findByRoleName} generates
 *       {@code SELECT * FROM roles WHERE role_name = ?}.</li>
 *   <li><strong>Optional return type with service usage</strong> — The
 *       {@link com.pkmprojects.shoppiq.service.role.RoleService} uses this method to look up
 *       seed roles ({@code ROLE_CUSTOMER}, {@code ROLE_SELLER}) that must exist in the database
 *       (populated by Flyway migration {@code V2__seed_roles.sql}).</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findByRoleName(String)
 *       → SELECT * FROM roles WHERE role_name = ?
 * </pre>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);
}
