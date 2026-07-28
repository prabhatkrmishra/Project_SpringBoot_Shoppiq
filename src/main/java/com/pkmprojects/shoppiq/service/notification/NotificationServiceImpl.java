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
 * <strong>Spring Boot Concept:</strong> Default implementation of {@link NotificationService}.
 *
 * <p><strong>What this Service implementation demonstrates:</strong></p>
 * <ul>
 *   <li><strong>Get-or-create pattern</strong> — The {@code getOrCreate} private method checks
 *       if a {@link com.pkmprojects.shoppiq.entity.notification.NotificationPreference} row
 *       exists for the user. If not, it creates one with default values (all flags enabled).
 *       This is a common pattern for optional one-to-one configurations.</li>
 *   <li><strong>Partial field update</strong> — {@link #updatePreferences} only sets fields
 *       where the request value is non-null. This allows clients to send only the fields
 *       they want to change without requiring the full current state.</li>
 *   <li><strong>{@code @Transactional}</strong> — Both public methods are transactional,
 *       ensuring entity changes are flushed atomically to the database.</li>
 *   <li><strong>Constructor injection with Lombok</strong> — Uses {@code @RequiredArgsConstructor}
 *       to generate the constructor for {@code NotificationPreferenceRepository}.</li>
 * </ul>
 *
 * @author prabhatkrmishra
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
