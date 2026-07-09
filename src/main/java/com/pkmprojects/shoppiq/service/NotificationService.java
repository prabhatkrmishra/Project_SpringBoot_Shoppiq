package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.notification.NotificationPreferenceResponse;
import com.pkmprojects.shoppiq.dto.notification.UpdateNotificationPreferenceRequest;
import com.pkmprojects.shoppiq.entity.User;

/**
 * Business contract for managing a user's email notification preferences.
 *
 * @author PrabhatKrMishra
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
