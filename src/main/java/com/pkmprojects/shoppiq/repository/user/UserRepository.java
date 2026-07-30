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
 * Persistence operations for the {@link User} aggregate.
 *
 * <p>Provides methods to query users by email, username, and various filters for authentication,
 * registration, and admin management. The repository supports paginated queries for user listing,
 * search by name/email/username, and atomic operations for account lockout management during
 * failed login attempts.</p>
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
     * @param name     the name to search for
     * @param pageable pagination information
     * @return page of matching users
     */
    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Finds users whose email contains the given string (case-insensitive).
     *
     * @param email    the email to search for
     * @param pageable pagination information
     * @return page of matching users
     */
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    /**
     * Finds users whose username contains the given string (case-insensitive).
     *
     * @param username the username to search for
     * @param pageable pagination information
     * @return page of matching users
     */
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    /**
     * Atomically increments failed login attempts and locks the account if threshold is reached.
     *
     * @param userId      the user ID
     * @param maxAttempts the maximum allowed failed attempts before lockout
     * @param lockoutTime the timestamp to set as lockout time
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
