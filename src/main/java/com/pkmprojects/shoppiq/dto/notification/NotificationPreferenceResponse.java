package com.pkmprojects.shoppiq.dto.notification;

import com.pkmprojects.shoppiq.entity.notification.NotificationPreference;

/**
 * Response payload describing a user's email notification preferences.
 *
 * <p>This record is a read-only projection of the user's notification
 * settings, returned by {@code GET /api/notification-preferences}. It
 * exposes four boolean preference flags that control which categories
 * of email notifications the user receives. The frontend uses this
 * data to render the notification preferences toggle switches in the
 * user's account settings page.</p>
 *
 * <p>All four preference fields are primitive booleans, which means they
 * always have a value (defaulting to {@code false} for new users). The
 * static {@link #from(NotificationPreference)} factory method maps
 * directly from the JPA entity, handling the user association
 * extraction.</p>
 *
 * @param userId            identifier of the user whose preferences these are
 * @param orderUpdates      whether the user wants to receive email
 *                          notifications for order status changes
 *                          (confirmation, shipping, delivery)
 * @param accountSecurity   whether the user wants to receive email
 *                          notifications for security events (password
 *                          changes, login from new device)
 * @param promotions        whether the user wants to receive promotional
 *                          emails (sales, new products, marketing campaigns)
 * @param reviewsEngagement whether the user wants to receive emails
 *                          about review activity (replies to their
 *                          reviews, review reminders)
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record NotificationPreferenceResponse(

        /**
         * User identifier.
         */
        Long userId,

        /**
         * Whether order update emails are enabled.
         */
        boolean orderUpdates,

        /**
         * Whether account security emails are enabled.
         */
        boolean accountSecurity,

        /**
         * Whether promotional emails are enabled.
         */
        boolean promotions,

        /**
         * Whether review engagement emails are enabled.
         */
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
