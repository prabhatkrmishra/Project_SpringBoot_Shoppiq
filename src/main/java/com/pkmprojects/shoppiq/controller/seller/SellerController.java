package com.pkmprojects.shoppiq.controller.seller;

import com.pkmprojects.shoppiq.dto.seller.request.SellerProfileUpdateRequest;
import com.pkmprojects.shoppiq.dto.seller.request.SellerRegistrationRequest;
import com.pkmprojects.shoppiq.dto.seller.response.SellerResponse;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.service.seller.SellerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <strong>Spring Boot Concept:</strong> REST controller for seller profile management.
 *
 * <p>Handles seller registration, profile retrieval, profile updates, account
 * deactivation, and store publication. Most endpoints resolve the seller from
 * the authenticated principal.</p>
 *
 * <p>Key design points:
 * <ul>
 *   <li><strong>Thin controller</strong> — no business logic; validates input and delegates to service layer.</li>
 *   <li><strong>Self-service</strong> — all operations act on the authenticated user's own seller profile only.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see SellerService
 * @since 1.0.0
 */
@RestController
@RequestMapping("/seller")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    /**
     * Registers a new seller application for the authenticated user.
     *
     * @param request     the seller registration payload
     * @param currentUser the authenticated user requesting seller status
     * @return 201 Created with the created seller profile
     */
    @PostMapping("/register")
    public ResponseEntity<SellerResponse> register(
            @Valid @RequestBody SellerRegistrationRequest request,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        SellerResponse response = sellerService.register(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns the authenticated seller's profile.
     *
     * @param currentUser the authenticated seller
     * @return 200 OK with the seller profile
     */
    @GetMapping("/profile")
    public ResponseEntity<SellerResponse> getProfile(
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        SellerResponse response = sellerService.getProfile(currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the authenticated seller's profile information.
     *
     * @param request     the updated profile data
     * @param currentUser the authenticated seller
     * @return 200 OK with the updated seller profile
     */
    @PutMapping("/update")
    public ResponseEntity<SellerResponse> updateProfile(
            @Valid @RequestBody SellerProfileUpdateRequest request,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        SellerResponse response = sellerService.updateProfile(request, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivates (soft-deletes) the authenticated seller's account.
     *
     * @param currentUser the authenticated seller
     * @return 200 OK
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteProfile(
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        sellerService.deleteProfile(currentUser);
        return ResponseEntity.ok().build();
    }

    /**
     * Publishes the seller's storefront, making it visible to customers.
     *
     * @param currentUser the authenticated seller
     * @return 200 OK
     */
    @PutMapping("/store/publish")
    public ResponseEntity<Void> publishStore(
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        sellerService.publishStore(currentUser);
        return ResponseEntity.ok().build();
    }
}