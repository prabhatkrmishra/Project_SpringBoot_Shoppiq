package com.crud.project.shoppiq.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity implementing {@link UserDetails} for Spring Security integration.
 *
 * <p>Serves as the single user representation across all authentication flows:
 * username/password login, Google OAuth2 login, and JWT token validation.
 * Implements {@link UserDetails} directly so that the entity can be used
 * as the principal in the SecurityContext without translation layers.</p>
 *
 * <h4>Unique constraints</h4>
 * <p>The {@code email} and {@code username} columns have database-level
 * unique constraints via {@code @Column(unique = true)}. These constraints
 * are the authoritative source of uniqueness — application-level checks
 * in controllers provide fast feedback but are not atomic under concurrent
 * access.</p>
 *
 * <h4>Token versioning</h4>
 * <p>The {@code tokenVersion} field enables centralized JWT invalidation.
 * Incrementing this value immediately invalidates all existing JWTs for
 * the user, enabling forced logout, password change invalidation, and
 * account disable. The JWT carries this version; if it doesn't match the
 * database value during validation, the token is rejected.</p>
 *
 * <h4>Account status</h4>
 * <p>The {@code enabled} field allows soft-disabling accounts without
 * deleting data. Disabled accounts cannot authenticate via any method.
 * The {@code isEnabled()} method is checked during JWT validation.</p>
 *
 * @see Role
 * @see com.crud.project.shoppiq.auth.utils.JwtAuthenticationUtils
 */
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails, Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Column
    @Getter
    @Setter
    private String name;

    @Column(nullable = false, unique = true)
    @Getter
    @Setter
    private String email;

    @Column(nullable = false, unique = true)
    @Getter
    @Setter
    private String username;

    @Column
    @Getter
    @Setter
    private String password;

    /**
     * Token version for JWT invalidation.
     * Incremented on password change, account disable, or forced logout.
     * The JWT carries this version; if it doesn't match the database value,
     * the token is rejected, forcing re-authentication.
     */
    @Column(nullable = false)
    @Getter
    @Setter
    private Integer tokenVersion = 0;

    /**
     * Whether the account is enabled. Disabled accounts cannot authenticate.
     * Checked during JWT validation to prevent disabled accounts from using
     * previously issued tokens until their token version is incremented.
     */
    @Column(nullable = false)
    @Getter
    @Setter
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "users_id"),
            inverseJoinColumns = @JoinColumn(name = "roles_id")
    )
    @Getter
    @Setter
    private Set<Role> roles = new HashSet<>();

    /**
     * Converts the user's Role entities into Spring Security GrantedAuthority
     * objects for role-based access control.
     *
     * <p>Called by Spring Security when evaluating {@code @PreAuthorize},
     * {@code hasRole()}, and other method-level security annotations.</p>
     *
     * @return collection of granted authorities derived from user roles
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}