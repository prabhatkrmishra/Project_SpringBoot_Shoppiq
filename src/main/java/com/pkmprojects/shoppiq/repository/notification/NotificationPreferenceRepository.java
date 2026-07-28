package com.pkmprojects.shoppiq.repository.notification;

import com.pkmprojects.shoppiq.entity.notification.NotificationPreference;
import com.pkmprojects.shoppiq.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link NotificationPreference} entities.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Entity association lookup</strong> — {@code findByUser(User)} navigates the
 *       {@code user} foreign key, generating {@code SELECT * FROM notification_preferences WHERE user_id = ?}.</li>
 *   <li><strong>Flat field lookup</strong> — {@code findByUserId(Long)} queries by the raw
 *       foreign-key column directly, showing both approaches for association traversal.</li>
 *   <li><strong>Optional return type</strong> — Both methods return
 *       {@link java.util.Optional}, which is the convention for derived queries that
 *       may return zero or one result.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findByUser(User)
 *       → SELECT * FROM notification_preferences WHERE user_id = ?
 *   findByUserId(Long)
 *       → SELECT * FROM notification_preferences WHERE user_id = ?
 * </pre>
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
