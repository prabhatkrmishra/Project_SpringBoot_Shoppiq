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
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for seller profile management.
 *
 * <p>Handles seller registration, profile retrieval, profile updates, account
 * deactivation, and storefront publication. New sellers register through this
 * controller and are placed in PENDING status until approved by an admin.
 * Once approved, sellers can manage their products and storefront.</p>
 *
 * <p>This controller acts as the HTTP boundary for seller profile operations.
 * It delegates all business logic — registration, profile CRUD, account
 * deactivation, and store publication — to {@link SellerService}. The controller
 * handles no business logic beyond extracting the authenticated principal.</p>
 *
 * <p>All endpoints resolve the seller from the authenticated principal. Most
 * endpoints require SELLER role. Registration is available to any authenticated
 * user.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * POST   /seller/register        — register as a new seller
 * GET    /seller/profile         — get the authenticated seller's profile
 * PUT    /seller/update          — update seller profile information
 * DELETE /seller/delete          — deactivate the seller account
 * PUT    /seller/store/publish   — publish the seller's storefront
 * </pre>
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
     * <p>The seller is placed in PENDING status until approved by an admin.
     * The authenticated user's existing roles are preserved.</p>
     *
     * @param request     the seller registration payload (validated via @Valid)
     * @param currentUser the authenticated user requesting seller status
     * @return 201 Created with the created seller profile response
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
     * @return 200 OK with the seller profile response
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
     * @param request     the updated profile data (validated via @Valid)
     * @param currentUser the authenticated seller
     * @return 200 OK with the updated seller profile response
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
     * <p>The seller's products are hidden from the storefront but the data
     * is retained for historical order records.</p>
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
     * <p>The seller must be in APPROVED status to publish their store.</p>
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