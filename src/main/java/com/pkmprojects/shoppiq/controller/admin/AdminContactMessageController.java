package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.dto.response.ContactMessageResponse;
import com.pkmprojects.shoppiq.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for admin contact message management.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/admin/messages")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminContactMessageController {

    private final ContactMessageService contactMessageService;

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("count", contactMessageService.countUnreadMessages()));
    }

    @GetMapping
    public ResponseEntity<List<ContactMessageResponse>> getAllMessages() {
        return ResponseEntity.ok(contactMessageService.getAllMessages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactMessageResponse> getMessageById(@PathVariable Long id) {
        return ResponseEntity.ok(contactMessageService.getMessageById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        contactMessageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ContactMessageResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(contactMessageService.markAsRead(id));
    }

    @PutMapping("/{id}/unread")
    public ResponseEntity<ContactMessageResponse> markAsUnread(@PathVariable Long id) {
        return ResponseEntity.ok(contactMessageService.markAsUnread(id));
    }
}
