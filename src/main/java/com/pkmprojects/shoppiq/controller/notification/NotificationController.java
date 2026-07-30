package com.pkmprojects.shoppiq.controller.notification;

import com.pkmprojects.shoppiq.dto.notification.NotificationPreferenceResponse;
import com.pkmprojects.shoppiq.dto.notification.UpdateNotificationPreferenceRequest;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.service.notification.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing authenticated user notification preferences.
 *
 * <p>Exposes endpoints under /user/notifications to read and update the current
 * user's email notification settings. Users can configure which types of email
 * notifications they receive, such as order updates and promotional content.
 * Preferences are stored per-user and persist across sessions.</p>
 *
 * <p>This controller acts as the HTTP boundary for notification preference
 * management. It delegates all business logic — preference retrieval, validation,
 * and persistence — to {@link NotificationService}. The controller handles no
 * business logic beyond extracting the authenticated principal.</p>
 *
 * <p>All endpoints require authentication (any role). The authenticated user is
 * resolved from the Spring Security context.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * GET    /user/notifications  — get current notification preferences
 * PUT    /user/notifications  — update notification preferences
 * </pre>
 *
 * @author prabhatkrmishra
 * @see NotificationService
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Returns the authenticated user's notification preferences.
     *
     * @param user the authenticated user resolved from the JWT
     * @return 200 OK with the current notification preferences
     */
    @GetMapping
    public ResponseEntity<NotificationPreferenceResponse> getPreferences(
            @AuthenticationPrincipal(expression = "user") User user
    ) {
        return ResponseEntity.ok(notificationService.getPreferences(user));
    }

    /**
     * Updates the authenticated user's notification preferences.
     *
     * @param user    the authenticated user resolved from the JWT
     * @param request the updated preference values (validated via @Valid)
     * @return 200 OK with the updated notification preferences
     */
    @PutMapping
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody UpdateNotificationPreferenceRequest request
    ) {
        return ResponseEntity.ok(notificationService.updatePreferences(user, request));
    }
}
