package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.notification.NotificationPreferenceResponse;
import com.pkmprojects.shoppiq.dto.notification.UpdateNotificationPreferenceRequest;
import com.pkmprojects.shoppiq.entity.NotificationPreference;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.repository.NotificationPreferenceRepository;
import com.pkmprojects.shoppiq.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link NotificationService}.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationPreferenceRepository preferenceRepository;

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getPreferences(User user) {
        NotificationPreference preference = getOrCreate(user);
        return NotificationPreferenceResponse.from(preference);
    }

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
