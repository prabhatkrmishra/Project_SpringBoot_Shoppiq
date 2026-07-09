package com.pkmprojects.shoppiq.dto.notification;

import com.pkmprojects.shoppiq.entity.NotificationPreference;

/**
 * Response payload describing a user's email notification preferences.
 *
 * @author PrabhatKrMishra
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
