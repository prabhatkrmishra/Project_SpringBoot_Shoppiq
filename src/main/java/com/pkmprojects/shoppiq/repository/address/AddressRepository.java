package com.pkmprojects.shoppiq.repository.address;

import com.pkmprojects.shoppiq.entity.address.Address;
import com.pkmprojects.shoppiq.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link Address} entities.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Derived query methods</strong> — Spring Data JPA parses method names like
 *       {@code findByUserAndIsDefaultTrue} and automatically generates the corresponding
 *       SQL: {@code SELECT * FROM addresses WHERE user_id = ? AND is_default = TRUE}.</li>
 *   <li><strong>{@code @Query} with JPQL</strong> — The {@link #clearDefaultForUser} method
 *       uses a custom JPQL {@code UPDATE} statement with {@code @Modifying}, teaching that
 *       derived method names cannot express bulk updates efficiently.</li>
 *   <li><strong>Interface-based repositories</strong> — No implementation needed; Spring
 *       provides the proxy at runtime via {@link org.springframework.data.jpa.repository.JpaRepository}.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findAllByUser(User)          → SELECT * FROM addresses WHERE user_id = ?
 *   findByUserAndIsDefaultTrue   → SELECT * FROM addresses WHERE user_id = ? AND is_default = TRUE
 * </pre>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Returns all addresses owned by the given user.
     *
     * @param user the owning user
     * @return list of addresses, possibly empty
     */
    List<Address> findAllByUser(User user);

    /**
     * Returns the default address for the given user, if one exists.
     *
     * @param user the owning user
     * @return optional default address
     */
    Optional<Address> findByUserAndIsDefaultTrue(User user);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user = :user")
    void clearDefaultForUser(@Param("user") User user);
}
