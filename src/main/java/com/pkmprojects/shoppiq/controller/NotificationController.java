package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.dto.notification.NotificationPreferenceResponse;
import com.pkmprojects.shoppiq.dto.notification.UpdateNotificationPreferenceRequest;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing a user's email notification preferences.
 *
 * @author PrabhatKrMishra
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

    @GetMapping
    public ResponseEntity<NotificationPreferenceResponse> getPreferences(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(notificationService.getPreferences(user));
    }

    @PutMapping
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateNotificationPreferenceRequest request
    ) {
        return ResponseEntity.ok(notificationService.updatePreferences(user, request));
    }
}
