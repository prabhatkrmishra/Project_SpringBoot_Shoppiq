package com.pkmprojects.shoppiq.entity.role;

import com.pkmprojects.shoppiq.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an application role for authorization within the Shoppiq
 * platform.
 *
 * <p>Each role has a unique {@code roleName} mapped to a Spring Security
 * {@code GrantedAuthority} (prefixed with {@code ROLE_}). Roles are assigned
 * to users through a many-to-many relationship and control access to
 * platform features, API endpoints, and administrative operations. The
 * role-based access model supports fine-grained permission control
 * across customer, seller, and administrator personas.</p>
 *
 * <p>Predefined roles include {@code ROLE_USER} for standard customers,
 * {@code ROLE_SELLER} for marketplace sellers, and {@code ROLE_ADMIN}
 * for platform administrators. Additional roles can be created through
 * the admin dashboard to support custom permission sets.</p>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.entity.user.User
 * @since 1.0.0
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AuditableEntity {

    /**
     * Name of this role (e.g. {@code ROLE_ADMIN}, {@code ROLE_SELLER},
     * {@code ROLE_USER}).
     *
     * <p>Mapped to a Spring Security {@code GrantedAuthority} with the
     * {@code ROLE_} prefix automatically applied by the framework. This
     * value is used in annotation-based security checks such as
     * {@code @PreAuthorize("hasRole('ADMIN'))"} and in method-level
     * authorization throughout the application.</p>
     */
    @Column
    String roleName;
}
