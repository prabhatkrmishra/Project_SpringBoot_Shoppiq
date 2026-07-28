package com.pkmprojects.shoppiq.controller.admin;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.contact.ContactMessageResponse;
import com.pkmprojects.shoppiq.service.contact.ContactMessageService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <strong>Spring Boot Concept:</strong> REST controller for admin contact message management.
 *
 * <p>Exposes endpoints to list, read, mark as read/unread, and delete
 * contact-form submissions submitted by site visitors. All endpoints require
 * {@code ADMIN} role and are mounted under {@code /api/admin/messages}.</p>
 *
 * <p>Key design points:
 * <ul>
 *   <li><strong>Thin controller</strong> — no business logic; validates input and delegates to service layer.</li>
 *   <li><strong>Unread count endpoint</strong> — provides a lightweight count for dashboard badges.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see ContactMessageService
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

    /**
     * Returns the count of unread contact messages.
     *
     * @return 200 OK with a map containing the unread count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<java.util.Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(java.util.Map.of("count", contactMessageService.countUnreadMessages()));
    }

    /**
     * Returns a paginated list of all contact messages.
     *
     * @param page zero-based page index
     * @param size page size (capped by {@code pagination.maxPageSize()})
     * @return 200 OK with page of messages
     */
    @GetMapping
    public ResponseEntity<PageResponse<ContactMessageResponse>> getAllMessages(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return ResponseEntity.ok(contactMessageService.getAllMessages(page, size));
    }

    /**
     * Returns a single contact message by ID.
     *
     * @param id the message ID
     * @return 200 OK with the message
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContactMessageResponse> getMessageById(@PathVariable Long id) {
        return ResponseEntity.ok(contactMessageService.getMessageById(id));
    }

    /**
     * Deletes a contact message.
     *
     * @param id the message ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        contactMessageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marks a contact message as read.
     *
     * @param id the message ID
     * @return 200 OK with the updated message
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ContactMessageResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(contactMessageService.markAsRead(id));
    }

    /**
     * Marks a contact message as unread.
     *
     * @param id the message ID
     * @return 200 OK with the updated message
     */
    @PutMapping("/{id}/unread")
    public ResponseEntity<ContactMessageResponse> markAsUnread(@PathVariable Long id) {
        return ResponseEntity.ok(contactMessageService.markAsUnread(id));
    }
}
