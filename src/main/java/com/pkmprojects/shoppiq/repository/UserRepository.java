package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}