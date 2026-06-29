package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.address.CreateAddressRequest;
import com.pkmprojects.shoppiq.dto.address.UpdateAddressRequest;
import com.pkmprojects.shoppiq.entity.User;

import java.util.List;

/**
 * Service for managing user shipping addresses.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface AddressService {

    /**
     * Creates a new address for the given user.
     *
     * @param user    the authenticated user
     * @param request address data
     * @return the created address
     */
    AddressResponse create(User user, CreateAddressRequest request);

    /**
     * Returns all addresses owned by the given user.
     *
     * @param user the authenticated user
     * @return list of addresses
     */
    List<AddressResponse> getAll(User user);

    /**
     * Returns a single address by ID, verifying ownership.
     *
     * @param user the authenticated user
     * @param id   address ID
     * @return the address
     */
    AddressResponse getById(User user, Long id);

    /**
     * Updates an existing address, verifying ownership.
     *
     * @param user    the authenticated user
     * @param id      address ID
     * @param request updated address data
     * @return the updated address
     */
    AddressResponse update(User user, Long id, UpdateAddressRequest request);

    /**
     * Deletes an address, verifying ownership.
     *
     * @param user the authenticated user
     * @param id   address ID
     */
    void delete(User user, Long id);

    /**
     * Marks the given address as the user's default, unsetting any previous default.
     *
     * @param user the authenticated user
     * @param id   address ID
     * @return the updated address
     */
    AddressResponse setDefault(User user, Long id);
}
