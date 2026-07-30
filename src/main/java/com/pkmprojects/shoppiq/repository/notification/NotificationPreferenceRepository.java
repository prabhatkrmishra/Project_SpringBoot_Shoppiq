package com.pkmprojects.shoppiq.repository.notification;

import com.pkmprojects.shoppiq.entity.notification.NotificationPreference;
import com.pkmprojects.shoppiq.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Persistence operations for the {@link NotificationPreference} aggregate.
 *
 * <p>Provides methods to query notification preferences by user for email delivery control.
 * The repository supports lookups by User entity or user ID for preference checking during
 * email sending operations.</p>
 *
 * @author prabhatkrmishra
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
