package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.address.CreateAddressRequest;
import com.pkmprojects.shoppiq.dto.address.UpdateAddressRequest;
import com.pkmprojects.shoppiq.entity.Address;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.AddressAccessDeniedException;
import com.pkmprojects.shoppiq.exception.AddressNotFoundException;
import com.pkmprojects.shoppiq.repository.AddressRepository;
import com.pkmprojects.shoppiq.service.AddressService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of {@link AddressService}.
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Uses constructor injection.</li>
 *     <li>All write operations run inside transactions.</li>
 *     <li>The one-default invariant is enforced via a bulk UPDATE before
 *         setting a new default, avoiding the need to load all addresses.</li>
 *     <li>Ownership is verified on every mutable operation.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    // ---------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>If {@code request.isDefault()} is {@code true}, any existing
     * default address for the user is unset before saving the new one.</p>
     */
    @Override
    public AddressResponse create(User user, CreateAddressRequest request) {

        if (request.isDefault()) {
            addressRepository.clearDefaultForUser(user);
        }

        Address address = Address.builder()
                .user(user)
                .label(request.label())
                .fullName(request.fullName())
                .phone(request.phone())
                .line1(request.line1())
                .line2(request.line2())
                .city(request.city())
                .state(request.state())
                .postalCode(request.postalCode())
                .country(request.country())
                .isDefault(request.isDefault())
                .build();

        return AddressResponse.from(addressRepository.save(address));
    }

    /** {@inheritDoc} */
    @Override
    public List<AddressResponse> getAll(User user) {
        return addressRepository.findAllByUser(user)
                .stream()
                .map(AddressResponse::from)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public AddressResponse getById(User user, Long id) {
        Address address = findAndValidateOwnership(user, id);
        return AddressResponse.from(address);
    }

    /**
     * {@inheritDoc}
     *
     * <p>If the request changes {@code isDefault} to {@code true}, any
     * existing default for the user is unset first.</p>
     */
    @Override
    public AddressResponse update(User user, Long id, UpdateAddressRequest request) {

        Address address = findAndValidateOwnership(user, id);

        if (request.isDefault() && !address.isDefault()) {
            addressRepository.clearDefaultForUser(user);
        }

        address.setLabel(request.label());
        address.setFullName(request.fullName());
        address.setPhone(request.phone());
        address.setLine1(request.line1());
        address.setLine2(request.line2());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country());
        address.setDefault(request.isDefault());

        return AddressResponse.from(addressRepository.save(address));
    }

    /** {@inheritDoc} */
    @Override
    public void delete(User user, Long id) {
        Address address = findAndValidateOwnership(user, id);
        addressRepository.delete(address);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Clears the current default for the user via a single bulk UPDATE
     * before setting the new one, guaranteeing the one-default invariant
     * even under concurrent requests.</p>
     */
    @Override
    public AddressResponse setDefault(User user, Long id) {

        Address address = findAndValidateOwnership(user, id);

        addressRepository.clearDefaultForUser(user);
        address.setDefault(true);

        return AddressResponse.from(addressRepository.save(address));
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /**
     * Loads an address by ID and verifies that it belongs to the given user.
     *
     * @param user the authenticated user
     * @param id   address ID
     * @return the address entity
     * @throws AddressNotFoundException    if no address with that ID exists
     * @throws AddressAccessDeniedException if the address belongs to another user
     */
    private Address findAndValidateOwnership(User user, Long id) {

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> AddressNotFoundException.id(id));

        if (!address.getUser().getId().equals(user.getId())) {
            throw AddressAccessDeniedException.forAddress(id);
        }

        return address;
    }
}
