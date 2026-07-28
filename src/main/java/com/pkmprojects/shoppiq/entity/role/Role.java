package com.pkmprojects.shoppiq.entity.role;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * <strong>Spring Boot Concept:</strong> JPA entity representing an application role for authorization.
 *
 * <p>Each role has a unique {@code roleName} which is mapped to a
 * Spring Security {@code GrantedAuthority} (prefixed with {@code ROLE_})
 * via the authority mapping infrastructure. Roles are assigned to users
 * and control access to endpoints and operations through
 * {@code @PreAuthorize} annotations.</p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Role-based access control (RBAC)</strong> — Used with
 *         Spring Security's {@code @PreAuthorize("hasRole('ADMIN')")} or
 *         {@code .hasRole("ADMIN")} in security configuration. The
 *         {@code roleName} gets the {@code ROLE_} prefix automatically
 *         when mapped to {@code GrantedAuthority}.</li>
 *     <li><strong>Many-to-Many with User</strong> — Roles are assigned to
 *         users via a join table ({@code user_roles}). The {@link User}
 *         entity holds the {@code @ManyToMany} mapping.</li>
 *     <li><strong>Simple reference entity</strong> — Minimal JPA entity
 *         with a single business field. Demonstrates that not every entity
 *         needs complex relationships or business logic.</li>
 *     <li><strong>Extends {@link AuditableEntity}</strong> — Inherits ID,
 *         version, and audit timestamps.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Entity
@Table(name="roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AuditableEntity {

    @Column
    String roleName;
}
