package com.pkmprojects.shoppiq.dto.user;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.entity.address.Address;
import com.pkmprojects.shoppiq.entity.user.User;

import java.time.Instant;

/**
 * Response payload for a created or retrieved user resource.
 *
 * <p>This record exposes the user's identifying information while
 * keeping internal persistence details hidden. It is returned by the
 * user registration, profile, and account endpoints. The DTO includes
 * an optional nested {@link AddressResponse} for the user's default
 * shipping address and a {@code hasPassword} flag for OAuth awareness.</p>
 *
 * <p>The static {@link #fromEntity(User)} factory method provides a
 * basic mapping without address or password flag, while the overloaded
 * {@link #of(User, Address, boolean)} method enriches the response
 * with the user's default address and password presence indicator.
 * The {@code hasPassword} field lets the frontend know whether the
 * user can use password-based login or was created via OAuth (Google,
 * GitHub).</p>
 *
 * @param id             unique identifier of the user account
 * @param name           full display name of the user
 * @param email          email address associated with the account
 * @param username       unique username used for authentication
 * @param createdAt      timestamp when the user account was first created
 * @param defaultAddress the user's default shipping address, or null
 *                       if no address has been saved yet
 * @param hasPassword    whether the account has a password set; false
 *                       for OAuth-only accounts that have never set a
 *                       password
 * @param emailVerified  whether the user's email address has been
 *                       verified through the verification flow
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record UserResponse(
        /**
         * Unique identifier of the user.
         */
        Long id,

        /**
         * Full name of the user.
         */
        String name,

        /**
         * Email address.
         */
        String email,

        /**
         * Username used during authentication.
         */
        String username,

        /**
         * Account creation timestamp.
         */
        Instant createdAt,

        /**
         * The user's default address, or {@code null} if none.
         */
        AddressResponse defaultAddress,

        /**
         * Whether the account has a password set (false for OAuth-only accounts).
         */
        boolean hasPassword,

        /**
         * Whether the user's email address has been verified.
         */
        boolean emailVerified
) {
    /**
     * Creates a basic response DTO from the given entity.
     *
     * @param user the user entity
     * @return populated response DTO (without address or password flag)
     */
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUsername(),
                user.getCreatedAt(),
                null,
                false,
                user.isEmailVerified()
        );
    }

    /**
     * Builds a response enriched with the user's default address and
     * password-presence flag.
     *
     * @param user           source entity
     * @param defaultAddress the user's default address, or {@code null}
     * @param hasPassword    whether the account has a password set
     * @return response DTO
     */
    public static UserResponse of(User user, Address defaultAddress, boolean hasPassword) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUsername(),
                user.getCreatedAt(),
                defaultAddress != null ? AddressResponse.from(defaultAddress) : null,
                hasPassword,
                user.isEmailVerified()
        );
    }
}
