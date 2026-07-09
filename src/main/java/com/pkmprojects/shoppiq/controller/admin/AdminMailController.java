package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.dto.admin.request.AdminMailRequest;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.AdminMailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * REST controller for admin mail functionality.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin/mail")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminMailController {

    private final UserRepository userRepository;
    private final AdminMailService adminMailService;

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchUsers(@RequestParam String q) {
        List<User> users;
        if (q.matches("\\d+")) {
            users = userRepository.findById(Long.parseLong(q))
                    .map(List::of)
                    .orElse(List.of());
        } else {
            List<User> byName = userRepository.findByNameContainingIgnoreCase(q);
            List<User> byEmail = userRepository.findByEmailContainingIgnoreCase(q);
            List<User> byUsername = userRepository.findByUsernameContainingIgnoreCase(q);

            users = Stream.of(byName, byEmail, byUsername)
                    .flatMap(List::stream)
                    .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a))
                    .values()
                    .stream()
                    .toList();
        }

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

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMail(@Valid @RequestBody AdminMailRequest request) {
        adminMailService.sendMail(request);
        return ResponseEntity.ok(Map.of("message", "Email sent successfully."));
    }
}
