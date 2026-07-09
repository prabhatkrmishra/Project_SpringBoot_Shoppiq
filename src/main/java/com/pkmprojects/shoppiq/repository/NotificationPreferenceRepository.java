package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.NotificationPreference;
import com.pkmprojects.shoppiq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for {@link NotificationPreference} entities.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    /**
     * Returns the preference row for the given user, if one exists.
     *
     * @param user the owning user
     * @return optional notification preference
     */
    Optional<NotificationPreference> findByUser(User user);

    /**
     * Returns the preference row for the given user id, if one exists.
     *
     * @param userId the owning user identifier
     * @return optional notification preference
     */
    Optional<NotificationPreference> findByUserId(Long userId);
}
