package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.role.Role;
import com.pkmprojects.shoppiq.entity.user.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO for admin customer management.
 *
 * <p>This record provides a comprehensive view of a user account for
 * administrators, including profile details, assigned roles, aggregate
 * order statistics, and account status. It is returned by the admin
 * user list and detail endpoints and is designed for the customer
 * management UI where administrators review, block, unblock, and
 * manage user accounts.</p>
 *
 * <p>The static {@link #fromEntity(User, long, BigDecimal)} factory
 * method accepts pre-computed aggregate statistics (order count and
 * total spend) that are joined from the order table at the repository
 * level. This avoids N+1 queries while keeping the DTO construction
 * logic centralized. The {@code enabled} flag controls whether the
 * user can authenticate; setting it to {@code false} effectively
 * blocks the account.</p>
 *
 * @param id          unique identifier of the user account
 * @param name        full display name of the user
 * @param username    unique username used for authentication
 * @param email       email address associated with the account
 * @param roles       list of role names assigned to this user (e.g. "ROLE_USER",
 *                    "ROLE_ADMIN"); determines access permissions
 * @param enabled     whether the account is currently enabled and can
 *                    authenticate; setting to false blocks the user
 * @param totalOrders total number of confirmed orders placed by this user
 * @param totalSpent  aggregate monetary value of all qualifying orders,
 *                    in the platform's base currency
 * @param createdAt   timestamp when the user account was first created
 * @param updatedAt   timestamp of the most recent modification to the account
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AdminUserResponse(

        /**
         * User identifier.
         */
        Long id,

        /**
         * Full name.
         */
        String name,

        /**
         * Username.
         */
        String username,

        /**
         * Email address.
         */
        String email,

        /**
         * Assigned roles.
         */
        List<String> roles,

        /**
         * Whether the account is enabled.
         */
        boolean enabled,

        /**
         * Total orders placed.
         */
        long totalOrders,

        /**
         * Total spent across all orders.
         */
        BigDecimal totalSpent,

        /**
         * Account creation timestamp.
         */
        Instant createdAt,

        /**
         * Last modification timestamp.
         */
        Instant updatedAt
) {

    /**
     * Creates an {@code AdminUserResponse} from a {@link User} entity with aggregated stats.
     *
     * @param user        user entity
     * @param totalOrders total orders count
     * @param totalSpent  total amount spent
     * @return mapped response DTO
     */
    public static AdminUserResponse fromEntity(
            User user,
            long totalOrders,
            BigDecimal totalSpent
    ) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getRoleName)
                .toList();

        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                roleNames,
                user.isEnabled(),
                totalOrders,
                totalSpent,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
