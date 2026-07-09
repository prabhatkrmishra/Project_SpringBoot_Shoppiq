package com.pkmprojects.shoppiq.dto.notification;

/**
 * Request payload for updating a user's email notification preferences.
 *
 * <p>All flags are optional so that a client may update a single
 * preference without resending the others.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record UpdateNotificationPreferenceRequest(

        Boolean orderUpdates,

        Boolean accountSecurity,

        Boolean promotions,

        Boolean reviewsEngagement
) {
}
