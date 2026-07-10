package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.dto.newsletter.NewsletterSubscribeRequest;
import com.pkmprojects.shoppiq.service.NewsletterSubscriberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for newsletter subscription management.
 *
 * <p>Public endpoints — no authentication required.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/newsletter")
public class NewsletterSubscriberController {

    private final NewsletterSubscriberService subscriberService;

    public NewsletterSubscriberController(NewsletterSubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(
            @Valid @RequestBody NewsletterSubscribeRequest request) {
        subscriberService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Successfully subscribed to the newsletter."));
    }

    @GetMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribe(@RequestParam String token) {
        subscriberService.unsubscribe(token);
        return ResponseEntity.ok(Map.of("message", "You have been unsubscribed from the newsletter."));
    }
}
