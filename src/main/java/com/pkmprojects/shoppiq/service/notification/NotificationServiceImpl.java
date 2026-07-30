package com.pkmprojects.shoppiq.service.notification;

import com.pkmprojects.shoppiq.dto.notification.NotificationPreferenceResponse;
import com.pkmprojects.shoppiq.dto.notification.UpdateNotificationPreferenceRequest;
import com.pkmprojects.shoppiq.entity.notification.NotificationPreference;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.repository.notification.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link NotificationService} implementation handling get-or-create preferences
 * and partial field updates for notification flags.
 *
 * @author prabhatkrmishra
 * @see NotificationService
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationPreferenceRepository preferenceRepository;

    /**
     * Retrieves or creates notification preferences for the user.
     *
     * <p>If no preference row exists, one is created with all flags enabled.</p>
     *
     * @param user authenticated user
     * @return notification preference response
     */
    @Override
    @Transactional
    public NotificationPreferenceResponse getPreferences(User user) {
        NotificationPreference preference = getOrCreate(user);
        return NotificationPreferenceResponse.from(preference);
    }

    /**
     * Partially updates the user's notification preferences.
     *
     * <p>Only fields with non-null values in the request are updated, allowing
     * clients to send only the fields they wish to change.</p>
     *
     * @param user    authenticated user
     * @param request partial update payload
     * @return updated notification preference response
     */
    @Override
    @Transactional
    public NotificationPreferenceResponse updatePreferences(User user, UpdateNotificationPreferenceRequest request) {
        NotificationPreference preference = getOrCreate(user);

        if (request.orderUpdates() != null) {
            preference.setOrderUpdates(request.orderUpdates());
        }
        if (request.accountSecurity() != null) {
            preference.setAccountSecurity(request.accountSecurity());
        }
        if (request.promotions() != null) {
            preference.setPromotions(request.promotions());
        }
        if (request.reviewsEngagement() != null) {
            preference.setReviewsEngagement(request.reviewsEngagement());
        }

        NotificationPreference saved = preferenceRepository.save(preference);
        return NotificationPreferenceResponse.from(saved);
    }

    private NotificationPreference getOrCreate(User user) {
        return preferenceRepository.findByUser(user)
                .orElseGet(() -> preferenceRepository.save(
                        NotificationPreference.builder()
                                .user(user)
                                .build()
                ));
    }
}
