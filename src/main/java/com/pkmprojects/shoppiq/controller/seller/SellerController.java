package com.pkmprojects.shoppiq.controller.seller;

import com.pkmprojects.shoppiq.dto.seller.request.SellerProfileUpdateRequest;
import com.pkmprojects.shoppiq.dto.seller.request.SellerRegistrationRequest;
import com.pkmprojects.shoppiq.dto.seller.response.SellerResponse;
import com.pkmprojects.shoppiq.entity.User;
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
 * REST controller for seller profile management.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Register a new seller application.</li>
 *     <li>Retrieve the authenticated seller's profile.</li>
 *     <li>Update the authenticated seller's profile.</li>
 *     <li>Delete (deactivate) the authenticated seller's account.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>All endpoints require authentication.</li>
 *     <li>The authenticated user is injected via {@link AuthenticationPrincipal}.</li>
 *     <li>Registration returns {@code 201 Created}.</li>
 *     <li>Profile retrieval, update, and delete return {@code 200 OK}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@RestController
@RequestMapping("/seller")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @PostMapping("/register")
    public ResponseEntity<SellerResponse> register(
            @Valid @RequestBody SellerRegistrationRequest request,
            @AuthenticationPrincipal User currentUser) {
        SellerResponse response = sellerService.register(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<SellerResponse> getProfile(
            @AuthenticationPrincipal User currentUser) {
        SellerResponse response = sellerService.getProfile(currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<SellerResponse> updateProfile(
            @Valid @RequestBody SellerProfileUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        SellerResponse response = sellerService.updateProfile(request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteProfile(
            @AuthenticationPrincipal User currentUser) {
        sellerService.deleteProfile(currentUser);
        return ResponseEntity.ok().build();
    }
}
