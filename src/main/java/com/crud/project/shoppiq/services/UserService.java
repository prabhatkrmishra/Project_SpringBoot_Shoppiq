package com.crud.project.shoppiq.services;

import com.crud.project.shoppiq.auth.dto.OAuthRegistrationSession;
import com.crud.project.shoppiq.dto.user.UserRequest;
import com.crud.project.shoppiq.exceptions.DuplicateUserException;
import com.crud.project.shoppiq.models.Role;
import com.crud.project.shoppiq.models.User;
import com.crud.project.shoppiq.repositories.RolesRepository;
import com.crud.project.shoppiq.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Service for user account management.
 *
 * <p>Handles user creation from both username/password registration and
 * Google OAuth2 registration. All creation methods are transactional and
 * rely on database-level unique constraints for definitive uniqueness
 * enforcement under concurrent access.</p>
 *
 * <p>Google OAuth2 users are created without a password — they authenticate
 * exclusively via Google OAuth2. This eliminates password storage liability
 * and credential-related attack vectors for OAuth2-only accounts.</p>
 *
 * <p>Database unique constraints on {@code email} and {@code username}
 * provide the authoritative uniqueness enforcement. Application-level
 * existence checks in controllers are not performed here; conflicts are
 * caught via {@link DataIntegrityViolationException}.</p>
 *
 * @see OAuthRegistrationSession
 * @see DuplicateUserException
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolesService rolesService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RolesRepository roleRepository, RolesService rolesService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolesService = rolesService;
    }

    /**
     * Creates a new user from username/password registration.
     *
     * <p>Password is hashed before storage. The CUSTOMER role is assigned
     * by default. Database constraints enforce email and username uniqueness.</p>
     *
     * @param newUserRequest contains name, email, username, and raw password
     * @return {@code true} if created successfully, {@code false} on constraint violation
     */
    @Transactional
    public boolean createUser(UserRequest newUserRequest) {
        try {
            User newUser = new User();
            newUser.setName(newUserRequest.getName());
            newUser.setEmail(newUserRequest.getEmail());
            newUser.setUsername(newUserRequest.getUsername());
            newUser.setPassword(passwordEncoder.encode(newUserRequest.getPassword()));
            newUser.setRoles(Set.of(rolesService.getCustomerRole()));

            userRepository.save(newUser);

            logger.info("User account created for username: {}", newUserRequest.getUsername());
            return true;
        } catch (DataIntegrityViolationException e) {
            logger.warn("User creation failed due to constraint violation for email: {} or username: {}",
                    newUserRequest.getEmail(), newUserRequest.getUsername());
            return false;
        }
    }

    /**
     * Creates a new user from Google OAuth2 registration.
     *
     * <p>The email and name come from the verified {@link OAuthRegistrationSession}
     * stored in the HTTP session. The username and password are chosen by the user
     * during the registration completion step. The password is BCrypt-hashed before
     * storage.</p>
     *
     * <p>This allows the user to authenticate via either Google OAuth2 or
     * username/password login in the future, providing flexibility without
     * forcing a single authentication method.</p>
     *
     * <p>The CUSTOMER role is assigned by default. Database unique constraints
     * on email and username provide the definitive duplicate protection.</p>
     *
     * @param oauthSession the verified Google profile from the session,
     *                     containing email, name, and authentication timestamp
     * @param username     the username chosen by the user
     * @param password     the raw password chosen by the user
     * @return the newly created User entity with CUSTOMER role assigned
     * @throws DuplicateUserException if the username or email conflicts with
     *                                an existing user (including concurrent insert)
     */
    @Transactional
    public User createGoogleUser(OAuthRegistrationSession oauthSession, String username, String password) {
        try {
            User user = new User();
            user.setName(oauthSession.name());
            user.setEmail(oauthSession.email());
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRoles(Set.of(rolesService.getCustomerRole()));

            User savedUser = userRepository.save(user);

            logger.info("Google OAuth2 user account created for username: {}", username);
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            logger.warn("Google user creation failed due to constraint violation for username: {}", username);
            throw new DuplicateUserException("Unable to create account");
        }
    }

    /**
     * Retrieves all users.
     *
     * @return list of all users, or empty list on error
     */
    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}