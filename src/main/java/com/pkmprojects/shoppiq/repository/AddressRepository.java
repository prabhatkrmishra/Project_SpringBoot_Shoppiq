package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Address;
import com.pkmprojects.shoppiq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Address} entities.
 *
 * @author PrabhatKrMishra
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

    /**
     * Returns {@code true} if the user already has a default address.
     *
     * @param user the owning user
     * @return {@code true} when a default exists
     */
    boolean existsByUserAndIsDefaultTrue(User user);

    /**
     * Clears the default flag on all addresses belonging to the given user.
     *
     * <p>Used before setting a new default to guarantee the one-default
     * invariant without loading all entities.</p>
     *
     * @param user the owning user
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user = :user")
    void clearDefaultForUser(@Param("user") User user);
}
