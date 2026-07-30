package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.dto.admin.request.AdminMailRequest;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.service.admin.AdminMailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for admin mail functionality.
 *
 * <p>Provides user search for recipient selection and mail-sending endpoints.
 * The search endpoint allows admins to find users by name, email, or username
 * when composing bulk or targeted emails. The send endpoint dispatches emails
 * either synchronously to specific recipients or asynchronously to all users.</p>
 *
 * <p>This controller acts as the HTTP boundary for admin mail operations. It
 * delegates all business logic — user search, email composition, and async
 * dispatch — to {@link AdminMailService}. The controller handles no business
 * logic beyond assembling the response map.</p>
 *
 * <p>All endpoints require ADMIN role and are mounted under /api/admin/mail.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * GET  /api/admin/mail/search  — search users by name, email, or username
 * POST /api/admin/mail/send    — send email to recipients or all users
 * </pre>
 *
 * @author prabhatkrmishra
 * @see AdminMailService
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin/mail")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminMailController {

    private final AdminMailService adminMailService;

    /**
     * Searches for users by name, email, or username for recipient selection.
     *
     * <p>Returns a lightweight summary of matching users (id, name, email,
     * username) suitable for rendering in a recipient picker UI.</p>
     *
     * @param q the search query string
     * @return 200 OK with list of matching user summaries
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchUsers(@RequestParam String q) {
        List<User> users = adminMailService.searchUsers(q);

        List<Map<String, Object>> results = users.stream()
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "username", user.getUsername()
                ))
                .toList();

        return ResponseEntity.ok(results);
    }

    /**
     * Sends an email to one or more recipients or to all users.
     *
     * <p>When sendToAll is enabled, the email is dispatched asynchronously
     * in the background and the endpoint returns immediately. Otherwise it
     * is sent synchronously to the specified recipients.</p>
     *
     * @param request the mail payload (subject, body, recipients, sendToAll flag)
     * @param admin   the authenticated admin used as the sender address
     * @return 200 OK with a status message
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMail(
            @Valid @RequestBody AdminMailRequest request,
            @AuthenticationPrincipal(expression = "user") User admin) {
        adminMailService.sendMail(request, admin.getEmail());
        if (Boolean.TRUE.equals(request.sendToAll())) {
            return ResponseEntity.ok(Map.of("message", "Email is being sent in the background. You can continue working."));
        }
        return ResponseEntity.ok(Map.of("message", "Email sent successfully."));
    }
}
