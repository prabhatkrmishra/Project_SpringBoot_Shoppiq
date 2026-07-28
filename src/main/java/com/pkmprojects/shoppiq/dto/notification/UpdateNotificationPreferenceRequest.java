package com.pkmprojects.shoppiq.dto.notification;

/**
 * <strong>Spring Boot Concept:</strong> Request payload for updating a user's email notification preferences.
 *
 * <p>All flags use {@code Boolean} (nullable wrapper type) instead of
 * {@code boolean} (primitive), making each field optional. This allows
 * the frontend to send a PATCH request with only the fields to change,
 * without including the current values of unchanged preferences.</p>
 *
 * <p><b>Partial update pattern:</b> Using nullable booleans in the request
 * DTO is a common approach for PATCH endpoints where the client sends
 * only the fields they want to modify. The service layer checks for
 * non-null values to determine which fields to update.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record UpdateNotificationPreferenceRequest(

        Boolean orderUpdates,

        Boolean accountSecurity,

        Boolean promotions,

        Boolean reviewsEngagement
) {
}
