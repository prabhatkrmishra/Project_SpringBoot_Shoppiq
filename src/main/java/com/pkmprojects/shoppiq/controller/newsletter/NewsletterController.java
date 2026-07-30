package com.pkmprojects.shoppiq.controller.newsletter;

import com.pkmprojects.shoppiq.dto.newsletter.NewsletterSubscribeRequest;
import com.pkmprojects.shoppiq.service.newsletter.NewsletterSubscriberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for public newsletter subscription and unsubscription.
 *
 * <p>Exposes two unauthenticated endpoints that allow site visitors to opt in
 * or out of marketing emails. The subscribe endpoint creates a subscriber record
 * and generates a signed unsubscription token. The unsubscribe endpoint uses
 * that token to deactivate the subscription without requiring authentication.</p>
 *
 * <p>This controller acts as the HTTP boundary for newsletter operations. It
 * delegates all business logic — subscriber persistence, token generation,
 * token validation, and subscription state management — to
 * {@link NewsletterSubscriberService}. The controller handles no business
 * logic beyond response assembly.</p>
 *
 * <p>No authentication is required. All endpoints are mounted under
 * /api/newsletter.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * POST   /api/newsletter/subscribe   — subscribe an email address
 * GET    /api/newsletter/unsubscribe — unsubscribe using a signed token
 * </pre>
 *
 * @author prabhatkrmishra
 * @see NewsletterSubscriberService
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {

    private final NewsletterSubscriberService subscriberService;

    public NewsletterController(NewsletterSubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    /**
     * Subscribes an email address to the newsletter.
     *
     * <p>Creates a new subscriber record and generates a signed
     * unsubscription token that is typically emailed to the subscriber.</p>
     *
     * @param request the subscription request containing the email address
     * @return 201 Created with a confirmation message
     */
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(
            @Valid @RequestBody NewsletterSubscribeRequest request) {
        subscriberService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Successfully subscribed to the newsletter."));
    }

    /**
     * Unsubscribes an email address using a signed token.
     *
     * <p>The token was generated at subscription time and sent via email.
     * This avoids needing authentication for unsubscription while still
     * preventing third-party unsubscription attacks.</p>
     *
     * @param token the signed unsubscription token emailed to the subscriber
     * @return 200 OK with a confirmation message
     */
    @GetMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribe(@RequestParam String token) {
        subscriberService.unsubscribe(token);
        return ResponseEntity.ok(Map.of("message", "You have been unsubscribed from the newsletter."));
    }
}
