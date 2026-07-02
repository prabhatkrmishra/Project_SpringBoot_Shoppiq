package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.dto.request.ContactMessageRequest;
import com.pkmprojects.shoppiq.dto.response.ContactMessageResponse;
import com.pkmprojects.shoppiq.service.ContactMessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for contact form submissions.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@RestController
@RequestMapping("/contact")
public class ContactController {

    private final ContactMessageService contactMessageService;

    public ContactController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    @PostMapping
    public ResponseEntity<ContactMessageResponse> submitMessage(
            @Valid @RequestBody ContactMessageRequest request) {
        ContactMessageResponse response = contactMessageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
