package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.response.ContactMessageResponse;
import com.pkmprojects.shoppiq.service.ContactMessageService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for admin contact message management.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/api/admin/messages")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminContactMessageController {

    private final ContactMessageService contactMessageService;
    private final PaginationProperties pagination;

    @GetMapping("/unread-count")
    public ResponseEntity<java.util.Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(java.util.Map.of("count", contactMessageService.countUnreadMessages()));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ContactMessageResponse>> getAllMessages(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return ResponseEntity.ok(contactMessageService.getAllMessages(page, size));
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
