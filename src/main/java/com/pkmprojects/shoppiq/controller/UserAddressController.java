package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.address.CreateAddressRequest;
import com.pkmprojects.shoppiq.dto.address.UpdateAddressRequest;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller that exposes address-management endpoints for authenticated
 * customers.
 *
 * <p>
 * All endpoints are scoped to {@code /user/address} and require the
 * {@code CUSTOMER} (or {@code ADMIN}) role. The authenticated user is resolved
 * from {@link AuthenticationPrincipal} and is never accepted from
 * client-supplied data.
 * </p>
 *
 * <h2>Endpoints</h2>
 * <ul>
 *     <li>{@code POST   /user/address/create}         — create a new address</li>
 *     <li>{@code GET    /user/address/get/all}        — list all addresses</li>
 *     <li>{@code GET    /user/address/get/{id}}       — get address by ID</li>
 *     <li>{@code PUT    /user/address/update/{id}}    — update an address</li>
 *     <li>{@code DELETE /user/address/delete/{id}}    — delete an address</li>
 *     <li>{@code PUT    /user/address/default/{id}}   — set default address</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/address")
public class UserAddressController {

    private final AddressService addressService;

    /**
     * Creates a new shipping address for the authenticated user.
     *
     * @param user    the authenticated user (from JWT)
     * @param request address data
     * @return the created address, HTTP 201
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateAddressRequest request
    ) {
        return addressService.create(user, request);
    }

    /**
     * Returns all addresses belonging to the authenticated user.
     *
     * @param user the authenticated user (from JWT)
     * @return list of addresses, HTTP 200
     */
    @GetMapping("/get/all")
    public List<AddressResponse> getAll(@AuthenticationPrincipal User user) {
        return addressService.getAll(user);
    }

    /**
     * Returns a single address by ID.
     *
     * @param user the authenticated user (from JWT)
     * @param id   address ID
     * @return the address, HTTP 200
     */
    @GetMapping("/get/{id}")
    public AddressResponse getById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        return addressService.getById(user, id);
    }

    /**
     * Updates an existing address.
     *
     * @param user    the authenticated user (from JWT)
     * @param id      address ID
     * @param request updated address data
     * @return the updated address, HTTP 200
     */
    @PutMapping("/update/{id}")
    public AddressResponse update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        return addressService.update(user, id, request);
    }

    /**
     * Deletes an address.
     *
     * @param user the authenticated user (from JWT)
     * @param id   address ID
     */
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        addressService.delete(user, id);
    }

    /**
     * Marks an address as the user's default, unsetting any previous default.
     *
     * @param user the authenticated user (from JWT)
     * @param id   address ID
     * @return the updated address, HTTP 200
     */
    @PutMapping("/default/{id}")
    public AddressResponse setDefault(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        return addressService.setDefault(user, id);
    }
}
