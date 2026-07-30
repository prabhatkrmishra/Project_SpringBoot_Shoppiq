package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import org.springframework.data.domain.Page;

import java.util.Optional;

/**
 * Read-only seller lookup facade for find, exists, count, and paging queries.
 *
 * <p>Decouples service-layer code from {@code SellerRepository},
 * providing seller resolution queries used across seller, admin, and user domains.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public interface SellerLookupService {

    /**
     * Finds a seller by the owning user's ID.
     */
    Optional<Seller> findByUserId(Long userId);

    /**
     * Finds a seller by primary key.
     */
    Optional<Seller> findById(Long sellerId);

    /**
     * Returns true if a seller profile exists for the given user.
     */
    boolean existsByUserId(Long userId);

    /**
     * Returns true if a seller with the given business email exists.
     */
    boolean existsByBusinessEmail(String email);

    /**
     * Returns all sellers with the given verification status, paginated.
     */
    Page<Seller> findByVerificationStatus(VerificationStatus status, int page, int size);

    /**
     * Returns all sellers, paginated.
     */
    Page<Seller> findAll(int page, int size);

    /**
     * Returns total seller count.
     */
    long count();
}
