package com.pkmprojects.shoppiq.dto.response;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.entity.Address;
import com.pkmprojects.shoppiq.entity.User;

import java.time.Instant;

/**
 * Response payload for a created {@link User} resource.
 *
 * <p>
 * Exposes the user's identifying information while keeping internal
 * persistence details hidden.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose user identifying information.</li>
 *     <li>Hide JPA entities from the REST API.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java record semantics.</li>
 *     <li>Created using {@link #fromEntity(User)} or {@link #of(User, Address, boolean)}.</li>
 *     <li>Does not expose the password or security tokens.</li>
 * </ul>
 *
 * @param id            user identifier
 * @param name          full name of the user
 * @param email         email address
 * @param username      username used during authentication
 * @param createdAt     account creation timestamp
 * @param defaultAddress the user's default address, or {@code null} if none
 * @param hasPassword   whether the account has a password set (false for OAuth-only accounts)
 * @param emailVerified whether the user's email address has been verified
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        String username,
        Instant createdAt,
        AddressResponse defaultAddress,
        boolean hasPassword,
        boolean emailVerified
) {
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
