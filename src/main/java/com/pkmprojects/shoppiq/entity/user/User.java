package com.pkmprojects.shoppiq.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pkmprojects.shoppiq.audit.AuditableEntity;
import com.pkmprojects.shoppiq.entity.address.Address;
import com.pkmprojects.shoppiq.entity.cart.Cart;
import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.entity.role.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents an authenticated Shoppiq user.
 *
 * <p>Serves as the single source of truth for user identity, profile data,
 * authentication credentials, and role assignments. A separate
 * {@link com.pkmprojects.shoppiq.auth.security.SecurityUser} adapter wraps
 * this entity for Spring Security integration. Each user is uniquely
 * identified by both email and username, either of which can be used for
 * login depending on the authentication flow configured.</p>
 *
 * <p>Supports JWT invalidation via token versioning, brute-force protection
 * through account lockout with a 30-minute soft timeout, and account
 * disabling without data deletion. The {@code tokenVersion} field allows
 * immediate revocation of all outstanding JWTs when a security event
 * occurs (e.g. password change, admin-initiated forced logout).</p>
 *
 * <p>A user may own multiple shipping {@link Address addresses}, submit
 * {@link ItemReview reviews}, and maintain a single shopping {@link Cart}.
 * The user also holds a set of {@link Role roles} that control access
 * to platform features through Spring Security's authority-based
 * authorization model.</p>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.auth.security.SecurityUser
 * @see Role
 * @see Cart
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
public class User extends AuditableEntity {

    /**
     * Full display name of the user, shown on invoices, profile pages,
     * and order confirmations.
     *
     * <p>Required field with a maximum length of 100 characters. This
     * is the human-readable identity used in emails, notifications, and
     * customer-facing UI throughout the platform.</p>
     */
    @NotBlank(message = "Name is required.")
    @Size(max = 100, message = "Name cannot exceed 100 characters.")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * User email address used for login, notifications, and order
     * communications.
     *
     * <p>This value must be globally unique across the entire platform.
     * Validated against RFC 5322 email format. The email is also used
     * as the primary channel for password reset, order updates, and
     * marketing communications.</p>
     */
    @Email(message = "Invalid email address.")
    @NotBlank(message = "Email is required.")
    @Size(max = 255)
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Unique username used during authentication and displayed in user
     * interfaces.
     *
     * <p>Must be between 3 and 50 characters. Unlike the email, the
     * username is intended for public-facing identification (e.g.
     * review authorship, seller profiles). The uniqueness constraint
     * is enforced at the database level.</p>
     */
    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Encoded password used for local authentication.
     *
     * <p>Passwords are stored only after hashing using the configured
     * {@code PasswordEncoder} (typically bcrypt). The raw password is
     * never persisted. This field is annotated with {@code @JsonIgnore}
     * to prevent accidental serialization in API responses. May be
     * {@code null} for users who authenticate exclusively via OAuth2
     * or other external providers.</p>
     */
    @JsonIgnore
    @Column(length = 255)
    private String password;

    /**
     * JWT token version used for immediate token invalidation.
     *
     * <p>Incrementing this value invalidates every existing JWT issued
     * to the user, enabling immediate revocation on security events
     * such as password changes or admin-forced logouts. The token
     * validation logic checks that the JWT's version claim matches
     * the current value stored here.</p>
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer tokenVersion = 0;

    /**
     * Indicates whether the account is enabled and permitted to log in.
     *
     * <p>Disabled accounts cannot authenticate even with valid credentials.
     * This flag is used for administrative account suspension without
     * deleting the user's data. Defaults to {@code true} for new
     * registrations.</p>
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Whether the user's email address has been verified through the
     * confirmation flow.
     *
     * <p>Email verification is required before certain actions such as
     * placing orders or submitting reviews. Defaults to {@code false}
     * for new registrations until the user completes the verification
     * process via the sent confirmation link.</p>
     */
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    /**
     * Timestamp when the email was successfully verified.
     *
     * <p>Populated once when the user clicks the verification link.
     * Remains {@code null} until verification is completed. Useful for
     * audit trails and for determining the age of the verified account
     * in analytics or compliance workflows.</p>
     */
    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    /**
     * Number of consecutive failed login attempts since the last
     * successful authentication.
     *
     * <p>Reset to 0 upon successful login. When this counter reaches
     * the configured threshold (typically 5), the account is locked
     * by setting {@link #lockoutTime}. This mechanism provides
     * brute-force protection without permanently blocking the user.</p>
     */
    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    /**
     * Timestamp when the account was locked due to too many consecutive
     * failed login attempts.
     *
     * <p>{@code null} means the account is not currently locked. The
     * lockout expires automatically after 30 minutes (soft lockout),
     * after which the user may attempt to log in again. The
     * {@link #isAccountNonLocked()} method checks whether the lockout
     * period has elapsed.</p>
     */
    @Column(name = "lockout_time")
    private Instant lockoutTime;

    /**
     * Security roles assigned to this user, controlling access to
     * platform features and administrative operations.
     *
     * <p>Roles are stored in a {@link Set} to prevent duplicates and
     * are eagerly fetched since the role collection is typically small
     * and required on every authenticated request. The join table
     * {@code user_roles} maintains the many-to-many association.
     * Duplicate additions are silently ignored due to Set semantics.</p>
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
     * Product reviews submitted by this user across the platform.
     *
     * <p>A user may review many products, but each product can only be
     * reviewed once per user (enforced at the service layer). Reviews
     * are automatically removed when the owning user is deleted via
     * cascade orphan removal, maintaining referential integrity without
     * requiring manual cleanup.</p>
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
     * Shipping addresses belonging to this user.
     *
     * <p>A user may have multiple shipping addresses (e.g. home, office),
     * each flagged with an {@code isDefault} indicator for quick checkout
     * selection. Addresses are cascade-deleted when the owning user is
     * removed, ensuring no orphaned address records remain in the system.</p>
     */
    @Builder.Default
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<Address> addresses = new ArrayList<>();

    /**
     * Shopping cart owned by this user.
     *
     * <p>A user has at most one cart, enforced by a unique constraint on
     * the {@code user_id} foreign key. The cart is created lazily when the
     * user first adds an item and is cascade-deleted with orphan removal
     * when the user is removed. The cart is excluded from JSON serialization
     * to prevent unnecessary data transfer in user-centric API responses.</p>
     */
    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Cart cart;

    /**
     * Indicates whether the user's account is locked.
     *
     * <p>An account is locked when {@link #lockoutTime} is set and fewer than
     * 30 minutes have elapsed since the lockout. After 30 minutes the
     * lockout expires automatically (soft lockout).</p>
     *
     * @return {@code true} if the account is not locked or the lockout has expired
     */
    public boolean isAccountNonLocked(Clock clock) {
        if (lockoutTime == null) {
            return true;
        }
        return lockoutTime.plus(Duration.ofMinutes(30)).isBefore(Instant.now(clock));
    }

    public boolean isAccountNonLocked() {
        return isAccountNonLocked(Clock.systemDefaultZone());
    }

    /**
     * Indicates whether the account is enabled.
     *
     * @return {@code true} if the account is enabled
     */
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
