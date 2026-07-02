package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO for admin customer management.
 *
 * <p>
 * This DTO provides a comprehensive view of a user for administrators,
 * including profile details, roles, order statistics, and account status.
 * Supports block/unblock operations.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose user details to admin API.</li>
 *     <li>Support account blocking/unblocking operations.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java Records.</li>
 *     <li>Includes aggregated order statistics.</li>
 *     <li>Created using {@link #fromEntity(User, long, long, BigDecimal)}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
     * @param user         user entity
     * @param totalOrders  total orders count
     * @param totalSpent   total amount spent
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