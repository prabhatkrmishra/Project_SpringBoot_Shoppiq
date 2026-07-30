package com.pkmprojects.shoppiq.dto.notification;

/**
 * Request payload for updating a user's email notification preferences.
 *
 * <p>This record uses nullable {@code Boolean} wrapper types instead of
 * primitive {@code boolean} for all fields, enabling partial updates
 * via PATCH semantics. The frontend sends only the fields the user
 * wants to change, leaving unchanged fields as {@code null}. The
 * service layer applies only non-null values, preserving the existing
 * state of any field not included in the request.</p>
 *
 * <p>No validation annotations are needed on the fields because
 * {@code Boolean} values are either a valid boolean or {@code null},
 * and {@code null} explicitly means "no change." This design avoids
 * the common pitfall of accidentally resetting preferences to
 * {@code false} when the frontend omits them from the request body.</p>
 *
 * @param orderUpdates      whether to receive order update emails;
 *                          {@code null} = no change to current setting
 * @param accountSecurity   whether to receive account security emails;
 *                          {@code null} = no change to current setting
 * @param promotions        whether to receive promotional emails;
 *                          {@code null} = no change to current setting
 * @param reviewsEngagement whether to receive review engagement emails;
 *                          {@code null} = no change to current setting
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record UpdateNotificationPreferenceRequest(

        /**
         * Whether to receive order update emails. Null = no change.
         */
        Boolean orderUpdates,

        /**
         * Whether to receive account security emails. Null = no change.
         */
        Boolean accountSecurity,

        /**
         * Whether to receive promotional emails. Null = no change.
         */
        Boolean promotions,

        /**
         * Whether to receive review engagement emails. Null = no change.
         */
        Boolean reviewsEngagement
) {
}
