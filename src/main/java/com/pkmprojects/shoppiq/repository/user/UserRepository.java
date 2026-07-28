package com.pkmprojects.shoppiq.repository.user;

import com.pkmprojects.shoppiq.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for User entity operations.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Derived queries (explicit prefix)</strong> — {@code findUserByEmail} and
 *       {@code findUserByUsername} use the explicit {@code findUserBy} prefix (as opposed to
 *       the shorter {@code findBy}), demonstrating both naming styles are valid.</li>
 *   <li><strong>{@code Top} / limit</strong> — {@code findTop10ByOrderByCreatedAtDesc} generates
 *       {@code SELECT * FROM users ORDER BY created_at DESC LIMIT 10}.</li>
 *   <li><strong>Paginated boolean filter</strong> — {@code findByEnabled(boolean, Pageable)}
 *       demonstrates filtering by a boolean field with automatic pagination.</li>
 *   <li><strong>Derived count queries</strong> — {@code countByEnabled} and
 *       {@code countByCreatedAtAfter} generate aggregate queries:
 *       {@code SELECT COUNT(*) FROM users WHERE enabled = ?} and
 *       {@code SELECT COUNT(*) FROM users WHERE created_at > ?}.</li>
 *   <li><strong>{@code ContainingIgnoreCase}</strong> — {@code findByNameContainingIgnoreCase},
 *       {@code findByEmailContainingIgnoreCase}, and {@code findByUsernameContainingIgnoreCase}
 *       generate case-insensitive LIKE searches using the standard pattern.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findUserByEmail(String)
 *       → SELECT * FROM users WHERE email = ?
 *   findUserByUsername(String)
 *       → SELECT * FROM users WHERE username = ?
 *   findTop10ByOrderByCreatedAtDesc
 *       → SELECT * FROM users ORDER BY created_at DESC LIMIT 10
 *   findByEnabled(boolean, Pageable)
 *       → SELECT * FROM users WHERE enabled = ? LIMIT ? OFFSET ?
 *   countByEnabled(boolean)
 *       → SELECT COUNT(*) FROM users WHERE enabled = ?
 *   countByCreatedAtAfter(Instant)
 *       → SELECT COUNT(*) FROM users WHERE created_at > ?
 *   findByNameContainingIgnoreCase(String)
 *       → SELECT * FROM users WHERE LOWER(name) LIKE LOWER(CONCAT('%', ?, '%'))
 * </pre>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email for OAuth2 account linking.
     *
     * @param email the email address to search for
     * @return the user if found
     */
    Optional<User> findUserByEmail(String email);

    /**
     * Finds a user by username for credential-based login.
     *
     * @param username the username to search for
     * @return the user if found
     */
    Optional<User> findUserByUsername(String username);

    /**
     * Returns the 10 most recently created users.
     *
     * @return list of recent users
     */
    List<User> findTop10ByOrderByCreatedAtDesc();

    /**
     * Returns a paginated view of all users filtered by enabled status.
     *
     * @param enabled  enabled filter
     * @param pageable pagination params
     * @return page of users
     */
    Page<User> findByEnabled(boolean enabled, Pageable pageable);

    /**
     * Counts users by enabled status.
     *
     * @param enabled enabled status
     * @return count of users
     */
    long countByEnabled(boolean enabled);

    /**
     * Counts users created after a given instant.
     *
     * @param instant start instant
     * @return count of users
     */
    long countByCreatedAtAfter(Instant instant);

    /**
     * Finds users whose name contains the given string (case-insensitive).
     *
     * @param name the name to search for
     * @return list of matching users
     */
    List<User> findByNameContainingIgnoreCase(String name);

    /**
     * Finds users whose email contains the given string (case-insensitive).
     *
     * @param email the email to search for
     * @return list of matching users
     */
    List<User> findByEmailContainingIgnoreCase(String email);

    /**
     * Finds users whose username contains the given string (case-insensitive).
     *
     * @param username the username to search for
     * @return list of matching users
     */
    List<User> findByUsernameContainingIgnoreCase(String username);

/**
     * Atomically increments failed login attempts and locks the account if threshold is reached.
     * This method prevents race conditions during concurrent login attempts.
     *
     * @param userId the user ID
     * @return number of rows updated (0 if already locked, 1 if successfully updated)
     */
    @Modifying
    @Query("""
            UPDATE User u
            SET u.failedLoginAttempts = u.failedLoginAttempts + 1,
                u.lockoutTime = CASE WHEN u.failedLoginAttempts + 1 >= :maxAttempts THEN :lockoutTime ELSE u.lockoutTime END
            WHERE u.id = :userId AND u.failedLoginAttempts < :maxAttempts""")
    int incrementFailedLoginAttemptsAndLockout(@Param("userId") Long userId,
                                               @Param("maxAttempts") int maxAttempts,
                                               @Param("lockoutTime") Instant lockoutTime);

    /**
     * Atomically unlocks the account by resetting failed login attempts and clearing lockout time.
     *
     * @param userId the user ID
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockoutTime = null WHERE u.id = :userId")
    int unlockAccount(@Param("userId") Long userId);
}
