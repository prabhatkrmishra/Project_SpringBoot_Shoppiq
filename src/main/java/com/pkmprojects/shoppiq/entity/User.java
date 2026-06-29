package com.pkmprojects.shoppiq.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents an authenticated Shoppiq user.
 *
 * <p>
 * This entity serves as the application's single source of truth for user
 * identity across all authentication mechanisms including username/password,
 * OAuth2 and JWT authentication.
 * </p>
 *
 * <p>
 * The entity implements {@link UserDetails}, allowing it to be used directly
 * by Spring Security without additional adapter classes.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Stores user profile information.</li>
 *     <li>Stores authentication credentials.</li>
 *     <li>Maintains role assignments.</li>
 *     <li>Supports JWT invalidation using token versioning.</li>
 *     <li>Owns reviews submitted by the user.</li>
 * </ul>
 *
 * <h2>Authentication</h2>
 * <ul>
 *     <li>Username & Password</li>
 *     <li>OAuth2 Login</li>
 *     <li>JWT Authentication</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Extends {@link AuditableEntity} to inherit persistence identity,
 *     optimistic locking and audit timestamps.</li>
 *     <li>Implements {@link UserDetails} for seamless Spring Security
 *     integration.</li>
 *     <li>Token versioning enables immediate JWT invalidation.</li>
 *     <li>Account disabling is supported without deleting user data.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @see JwtAuthenticationUtils
 * @see Role
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends AuditableEntity
        implements UserDetails, Serializable {

    /**
     * Full name of the user.
     */
    @NotBlank(message = "Name is required.")
    @Size(max = 100, message = "Name cannot exceed 100 characters.")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * User email address.
     *
     * <p>
     * This value must be globally unique.
     * </p>
     */
    @Email(message = "Invalid email address.")
    @NotBlank(message = "Email is required.")
    @Size(max = 255)
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Username used during authentication.
     */
    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Encoded password.
     *
     * <p>
     * Passwords are stored only after hashing using the configured
     * PasswordEncoder.
     * </p>
     */
    @Column(length = 255)
    private String password;

    /**
     * JWT token version.
     *
     * <p>
     * Incrementing this value invalidates every existing JWT issued to
     * the user.
     * </p>
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer tokenVersion = 0;

    /**
     * Indicates whether the account is enabled.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Security roles assigned to this user.
     */
    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(
                    name = "user_id",
                    foreignKey = @ForeignKey(name = "fk_user_roles_user")
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id",
                    foreignKey = @ForeignKey(name = "fk_user_roles_role")
            )
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Product reviews submitted by this user.
     *
     * <p>
     * A user may review many products. Reviews are automatically removed
     * when the owning user is deleted.
     * </p>
     */
    @Builder.Default
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<ItemReview> itemReviews = new ArrayList<>();

    /**
     * Shopping cart owned by this user.
     *
     * <p>
     * A user has at most one cart. The cart is created lazily when the
     * user first adds an item.
     * </p>
     */
    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Cart cart;

    /**
     * Converts the user's assigned roles into Spring Security authorities.
     *
     * <p>
     * Spring Security invokes this method during authentication and
     * authorization to determine the permissions granted to the current user.
     * Each {@link Role} is converted into a {@link SimpleGrantedAuthority}.
     * </p>
     *
     * @return immutable collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .toList();
    }

    /**
     * Indicates whether the user's account has expired.
     *
     * <p>
     * Shoppiq currently does not expire user accounts.
     * </p>
     *
     * @return always {@code true}
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user's account is locked.
     *
     * <p>
     * Account locking is not currently implemented.
     * </p>
     *
     * @return always {@code true}
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials have expired.
     *
     * <p>
     * Credential expiration is not currently enforced.
     * </p>
     *
     * @return always {@code true}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the account is enabled.
     *
     * <p>
     * Disabled users cannot authenticate regardless of credential validity.
     * JWT validation also respects this flag.
     * </p>
     *
     * @return {@code true} if the account is enabled
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Adds a role to this user.
     *
     * <p>
     * Duplicate roles are ignored because roles are stored in a {@link Set}.
     * </p>
     *
     * @param role role to assign
     */
    public void addRole(Role role) {

        if (role == null) {
            return;
        }

        roles.add(role);
    }

    /**
     * Removes a role from this user.
     *
     * @param role role to remove
     */
    public void removeRole(Role role) {

        if (role == null) {
            return;
        }

        roles.remove(role);
    }

    /**
     * Associates a review with this user.
     *
     * <p>
     * Maintains both sides of the bidirectional relationship.
     * </p>
     *
     * @param review review to associate
     */
    public void addReview(ItemReview review) {

        if (review == null) {
            return;
        }

        itemReviews.add(review);
        review.setUser(this);
    }

    /**
     * Removes a review written by this user.
     *
     * <p>
     * Maintains both sides of the bidirectional relationship.
     * </p>
     *
     * @param review review to remove
     */
    public void removeReview(ItemReview review) {

        if (review == null) {
            return;
        }

        itemReviews.remove(review);
        review.setUser(null);
    }
}