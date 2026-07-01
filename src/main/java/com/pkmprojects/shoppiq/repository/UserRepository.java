package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity operations.
 *
 * <p>Provides database access for user lookups during authentication,
 * registration, and JWT validation. The {@code findById} method is used
 * by the JWT filter to load users for token version verification.
 * The {@code findUserByEmail} method supports OAuth2 account linking.
 * The {@code findUserByUsername} method supports username/password login.</p>
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
     * @return count of new users
     */
    long countByCreatedAtAfter(Instant instant);
}