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
 * <strong>Spring Boot Concept:</strong> REST controller for admin mail functionality.
 *
 * <p>Provides user search (for recipient selection) and mail-sending endpoints.
 * All endpoints require {@code ADMIN} role and are mounted under
 * {@code /api/admin/mail}.</p>
 *
 * <p>Key design points:
 * <ul>
 *   <li><strong>Thin controller</strong> — no business logic; validates input and delegates to service layer.</li>
 *   <li><strong>Background sending</strong> — bulk mail to "all users" is dispatched asynchronously in the service layer.</li>
 * </ul>
 * </p>
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
     * @param q the search query string
     * @return 200 OK with list of matching user summaries (id, name, email, username)
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
     * Sends an email to one or more recipients (or all users).
     *
     * <p>When {@code sendToAll} is enabled, the email is dispatched
     * asynchronously in the background. Otherwise it is sent synchronously
     * to the specified recipients.</p>
     *
     * @param request the mail payload (subject, body, recipients)
     * @param admin   the authenticated admin (used as sender)
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
