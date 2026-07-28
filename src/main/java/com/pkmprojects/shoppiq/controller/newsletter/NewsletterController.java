package com.pkmprojects.shoppiq.controller.newsletter;

import com.pkmprojects.shoppiq.dto.newsletter.NewsletterSubscribeRequest;
import com.pkmprojects.shoppiq.service.newsletter.NewsletterSubscriberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <strong>Spring Boot Concept:</strong> REST controller for public newsletter
 * subscription and unsubscription.
 *
 * <p>Exposes two unauthenticated endpoints — {@code POST /api/newsletter/subscribe}
 * and {@code GET /api/newsletter/unsubscribe} — that allow site visitors to
 * opt in or out of marketing emails. Delegates all business logic to
 * {@link NewsletterSubscriberService}.</p>
 *
 * <p>Key design points:
 * <ul>
 *   <li><strong>Public (unauthenticated) endpoints</strong> — no
 *       {@code @PreAuthorize} or security constraints.</li>
 *   <li><strong>Token-based unsubscription</strong> — the unsubscribe flow
 *       uses a signed token (rather than authentication) to securely identify
 *       the subscriber, a common pattern in email-based workflows.</li>
 * </ul>
 * </p>
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
     * <h4>Request flow:</h4>
     * <ol>
     *   <li>Validate the {@link NewsletterSubscribeRequest} (email format).</li>
     *   <li>Delegate to {@link NewsletterSubscriberService#subscribe} which
     *       persists the subscriber and generates an unsubscribe token.</li>
     *   <li>Return HTTP 201 with a confirmation message.</li>
     * </ol>
     *
     * @param request the subscription request containing the email address
     * @return HTTP 201 with a success message
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
     * <h4>Request flow:</h4>
     * <ol>
     *   <li>Receive the signed token as a query parameter.</li>
     *   <li>Delegate to {@link NewsletterSubscriberService#unsubscribe} which
     *       validates the token and marks the subscriber as inactive.</li>
     *   <li>Return HTTP 200 with a confirmation message.</li>
     * </ol>
     *
     * @param token the signed unsubscription token emailed to the subscriber
     * @return HTTP 200 with a success message
     */
    @GetMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribe(@RequestParam String token) {
        subscriberService.unsubscribe(token);
        return ResponseEntity.ok(Map.of("message", "You have been unsubscribed from the newsletter."));
    }
}
