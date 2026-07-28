package com.pkmprojects.shoppiq.service.notification;

import com.pkmprojects.shoppiq.dto.notification.NotificationPreferenceResponse;
import com.pkmprojects.shoppiq.dto.notification.UpdateNotificationPreferenceRequest;
import com.pkmprojects.shoppiq.entity.user.User;

/**
 * <strong>Spring Boot Concept:</strong> Business contract for managing a user's email notification preferences.
 *
 * <p><strong>What the Service layer demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Interface-first design</strong> — Defines the contract for notification
 *       preference management, allowing the implementation to be swapped or mocked.</li>
 *   <li><strong>Auto-creation pattern</strong> — {@link #getPreferences} creates a default
 *       preference row (all flags enabled) if none exists, teaching the "get-or-create"
 *       pattern common in Spring Boot service layers.</li>
 *   <li><strong>Partial update pattern</strong> — {@link #updatePreferences} applies only
 *       non-null fields from the request, demonstrating a merge/partial-update strategy
 *       at the service layer.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface NotificationService {

    /**
     * Returns the notification preferences for the given user.
     *
     * <p>If no preference row exists yet, one is created with all flags
     * enabled by default.</p>
     *
     * @param user the requesting user
     * @return the user's notification preferences
     */
    NotificationPreferenceResponse getPreferences(User user);

    /**
     * Updates the notification preferences for the given user.
     *
     * <p>Only the flags present (non-null) in the request are applied;
     * null flags leave the existing value unchanged.</p>
     *
     * @param user    the requesting user
     * @param request the preferences to update
     * @return the updated notification preferences
     */
    NotificationPreferenceResponse updatePreferences(User user, UpdateNotificationPreferenceRequest request);
}
