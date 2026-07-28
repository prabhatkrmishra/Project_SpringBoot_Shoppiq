package com.pkmprojects.shoppiq.dto.notification;

import com.pkmprojects.shoppiq.entity.notification.NotificationPreference;

/**
 * <strong>Spring Boot Concept:</strong> Response payload describing a user's email notification preferences.
 *
 * <p>This Java record is a read-only projection of a user's notification
 * settings. All four preference fields are booleans — the simplest validation
 * case (no annotations needed since booleans always have a value).</p>
 *
 * <p><b>Mapping pattern:</b> The {@link #from(com.pkmprojects.shoppiq.entity.notification.NotificationPreference) from()}
 * method extracts the user ID from the parent entity and maps each boolean
 * flag, demonstrating entity-to-DTO conversion for a one-to-one relationship.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record NotificationPreferenceResponse(

        Long userId,

        boolean orderUpdates,

        boolean accountSecurity,

        boolean promotions,

        boolean reviewsEngagement
) {

    /**
     * Builds a response from an entity.
     *
     * @param preference source entity
     * @return response DTO
     */
    public static NotificationPreferenceResponse from(NotificationPreference preference) {
        return new NotificationPreferenceResponse(
                preference.getUser().getId(),
                preference.isOrderUpdates(),
                preference.isAccountSecurity(),
                preference.isPromotions(),
                preference.isReviewsEngagement()
        );
    }
}
