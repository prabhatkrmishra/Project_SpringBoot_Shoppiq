package com.pkmprojects.shoppiq.controller.contact;

import com.pkmprojects.shoppiq.dto.contact.ContactMessageRequest;
import com.pkmprojects.shoppiq.dto.contact.ContactMessageResponse;
import com.pkmprojects.shoppiq.service.contact.ContactMessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <strong>Spring Boot Concept:</strong> REST controller handling public contact-form
 * submissions.
 *
 * <p>Exposes a single {@code POST /contact} endpoint that accepts visitor
 * inquiries and delegates to {@link ContactMessageService} for persistence
 * and notification. No authentication is required — anyone can submit the
 * contact form.</p>
 *
 * <p>Key design points:
 * <ul>
 *   <li><strong>Thin controller</strong> — validates the incoming {@code @Valid}
 *       request body and immediately delegates to the service layer.</li>
 *   <li><strong>Public unauthenticated endpoint</strong> — no
 *       {@code @PreAuthorize} restriction, demonstrating how to selectively
 *       expose open endpoints alongside secured ones.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see ContactMessageService
 * @since 1.0.0
 */
@RestController
@RequestMapping("/contact")
public class ContactController {

    private final ContactMessageService contactMessageService;

    public ContactController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    /**
     * Submits a new contact/inquiry message from a site visitor.
     *
     * <p>The message is persisted via {@link ContactMessageService#create} and
     * becomes visible in the admin messages panel. No email notification is
     * sent from this controller; the service layer may trigger one
     * asynchronously.</p>
     *
     * <h4>Request flow:</h4>
     * <ol>
     *   <li>Validate the {@link ContactMessageRequest} payload (name, email,
     *       subject, message body).</li>
     *   <li>Delegate to {@link ContactMessageService#create} which persists
     *       the message.</li>
     *   <li>Return HTTP 201 with the created {@link ContactMessageResponse}.</li>
     * </ol>
     *
     * @param request the validated contact-message payload
     * @return HTTP 201 with the newly created message response
     */
    @PostMapping
    public ResponseEntity<ContactMessageResponse> submitMessage(
            @Valid @RequestBody ContactMessageRequest request) {
        ContactMessageResponse response = contactMessageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
