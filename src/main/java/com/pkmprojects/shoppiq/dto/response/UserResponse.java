package com.pkmprojects.shoppiq.dto.response;

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
 *     <li>Created using {@link #fromEntity(User)}.</li>
 *     <li>Does not expose the password or security tokens.</li>
 * </ul>
 *
 * @param id        user identifier
 * @param name      full name of the user
 * @param email     email address
 * @param username  username used during authentication
 * @param createdAt account creation timestamp
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        String username,
        Instant createdAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUsername(),
                user.getCreatedAt()
        );
    }
}
