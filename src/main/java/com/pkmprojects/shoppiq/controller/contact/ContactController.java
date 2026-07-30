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
 * REST controller handling public contact-form submissions.
 *
 * <p>Exposes a single unauthenticated endpoint that accepts visitor inquiries
 * through the site's contact form. Submissions are persisted and become visible
 * in the admin messages panel for follow-up. No email notification is sent
 * directly from this controller; the service layer may trigger one asynchronously.</p>
 *
 * <p>This controller acts as the HTTP boundary for contact form intake. It
 * delegates all business logic — message persistence, validation, and any
 * asynchronous notification — to {@link ContactMessageService}. The controller
 * handles no business logic beyond request validation and response assembly.</p>
 *
 * <p>No authentication is required. The endpoint is mounted under /contact.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * POST   /contact  — submit a new contact/inquiry message
 * </pre>
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
     * <p>The message is persisted via ContactMessageService and becomes
     * visible in the admin messages panel. No email notification is sent
     * from this controller; the service layer may trigger one asynchronously.</p>
     *
     * @param request the validated contact-message payload
     * @return 201 Created with the newly created message response
     */
    @PostMapping
    public ResponseEntity<ContactMessageResponse> submitMessage(
            @Valid @RequestBody ContactMessageRequest request) {
        ContactMessageResponse response = contactMessageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
