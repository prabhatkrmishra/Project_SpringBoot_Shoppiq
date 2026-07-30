package com.pkmprojects.shoppiq.controller.address;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.address.CreateAddressRequest;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.service.address.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for authenticated customer address management.
 *
 * <p>Provides endpoints for creating, reading, updating, and deleting shipping
 * addresses associated with the authenticated user's account. Each user maintains
 * their own address book and can designate one address as the default for
 * checkout. Ownership is enforced at the service layer — users can only access
 * their own addresses.</p>
 *
 * <p>This controller acts as the HTTP boundary for address operations. It
 * delegates all business logic — persistence, ownership validation, default
 * address management, and deletion — to {@link AddressService}. The controller
 * handles no business logic beyond extracting the authenticated principal.</p>
 *
 * <p>All endpoints are scoped to /user/address and require CUSTOMER or ADMIN
 * role. The authenticated user is resolved from AuthenticationPrincipal and is
 * never accepted from client-supplied data.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * POST   /user/address/create         — create a new address
 * GET    /user/address/get/all        — list all addresses
 * GET    /user/address/get/{id}       — get address by ID
 * PUT    /user/address/update/{id}    — update an address
 * DELETE /user/address/delete/{id}    — delete an address
 * PUT    /user/address/default/{id}   — set default address
 * </pre>
 *
 * @author prabhatkrmishra
 * @see AddressService
 * @since 1.0.0
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/address")
public class AddressController {

    private final AddressService addressService;

    /**
     * Creates a new shipping address for the authenticated user.
     *
     * <p>The address is associated with the authenticated user's account.
     * If this is the user's first address, it is automatically set as
     * the default.</p>
     *
     * @param user    the authenticated user resolved from the JWT
     * @param request the address data payload (validated via @Valid)
     * @return 201 Created with the created address response
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody CreateAddressRequest request
    ) {
        return addressService.create(user, request);
    }

    /**
     * Returns all addresses belonging to the authenticated user.
     *
     * @param user the authenticated user resolved from the JWT
     * @return 200 OK with list of address responses
     */
    @GetMapping("/get/all")
    public List<AddressResponse> getAll(@AuthenticationPrincipal(expression = "user") User user) {
        return addressService.getAll(user);
    }

    /**
     * Returns a single address by ID, scoped to the authenticated user.
     *
     * @param user the authenticated user resolved from the JWT
     * @param id   the address ID to retrieve
     * @return 200 OK with the address response
     */
    @GetMapping("/get/{id}")
    public AddressResponse getById(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable Long id
    ) {
        return addressService.getById(user, id);
    }

    /**
     * Updates an existing address.
     *
     * @param user    the authenticated user resolved from the JWT
     * @param id      the address ID to update
     * @param request the updated address data payload (validated via @Valid)
     * @return 200 OK with the updated address response
     */
    @PutMapping("/update/{id}")
    public AddressResponse update(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable Long id,
            @Valid @RequestBody CreateAddressRequest request
    ) {
        return addressService.update(user, id, request);
    }

    /**
     * Deletes an address belonging to the authenticated user.
     *
     * @param user the authenticated user resolved from the JWT
     * @param id   the address ID to delete
     */
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable Long id
    ) {
        addressService.delete(user, id);
    }

    /**
     * Marks an address as the user's default, unsetting any previous default.
     *
     * @param user the authenticated user resolved from the JWT
     * @param id   the address ID to set as default
     * @return 200 OK with the updated address response
     */
    @PutMapping("/default/{id}")
    public AddressResponse setDefault(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable Long id
    ) {
        return addressService.setDefault(user, id);
    }
}
