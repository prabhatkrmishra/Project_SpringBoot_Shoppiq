package com.pkmprojects.shoppiq.controller.notification;

import com.pkmprojects.shoppiq.dto.notification.NotificationPreferenceResponse;
import com.pkmprojects.shoppiq.dto.notification.UpdateNotificationPreferenceRequest;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.service.notification.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * <strong>Spring Boot Concept:</strong> REST controller for managing authenticated
 * user notification preferences.
 *
 * <p>Exposes endpoints under {@code /user/notifications} to read and update
 * the current user's email notification settings. All endpoints require
 * authentication via {@code @PreAuthorize("isAuthenticated()")} and resolve the
 * user from the Spring Security context.</p>
 *
 * <p>Key design points:
 * <ul>
 *   <li><strong>Thin controller</strong> — no business logic; validates input and delegates to service layer.</li>
 *   <li><strong>User-specific data scoping</strong> — the controller never
 *       accepts a user ID from the client; the authenticated principal is the
 *       only identity used, preventing horizontal privilege escalation.</li>
 * </ul>
 * </p>
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
     * @param user the authenticated user (from JWT)
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
     * @param user    the authenticated user (from JWT)
     * @param request the updated preference values
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
