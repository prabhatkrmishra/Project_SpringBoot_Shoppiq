package com.pkmprojects.shoppiq.service.address;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.address.CreateAddressRequest;
import com.pkmprojects.shoppiq.entity.user.User;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Service interface for managing user shipping addresses.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * This interface defines the <strong>Service layer</strong> contract for address management.
 * In the standard {@code Controller → Service → Repository} pattern:
 * </p>
 * <ul>
 *   <li><strong>Controller</strong> receives HTTP requests and delegates to this service.</li>
 *   <li><strong>Service</strong> (this interface) defines the business operations.</li>
 *   <li><strong>Repository</strong> handles database persistence (hidden behind the implementation).</li>
 * </ul>
 *
 * <h2>What is {@code @Service}?</h2>
 * <p>
 * {@code @Service} is a Spring Stereotype annotation (specialization of {@code @Component})
 * that marks a class as a <strong>Service</strong> in the business layer. Spring automatically
 * discovers these classes via component-scanning and registers them as beans in the
 * application context, making them available for dependency injection into controllers
 * and other services.
 * </p>
 *
 * <h2>Interface-Segregation Pattern</h2>
 * <p>
 * Separating the contract (interface) from the implementation allows:
 * <ul>
 *   <li>Loose coupling between layers — controllers depend on abstractions, not concrete classes.</li>
 *   <li>Easier testing — mock implementations can be substituted.</li>
 *   <li>Multiple implementations if needed (e.g., a stub for testing).</li>
 * </ul>
 * </p>
 *
 * <h2>Business Logic Responsibilities</h2>
 * <ul>
 *   <li>Create new shipping addresses for authenticated users.</li>
 *   <li>Retrieve addresses with ownership verification (users can only see their own addresses).</li>
 *   <li>Update existing addresses (ownership-gated).</li>
 *   <li>Delete addresses (ownership-gated).</li>
 *   <li>Enforce the "one default address per user" invariant.</li>
 * </ul>
 *
 * @author prabhatkrmishra
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
    AddressResponse update(User user, Long id, CreateAddressRequest request);

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
