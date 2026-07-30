package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.repository.seller.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * {@link SellerLookupService} implementation providing read-only seller queries.
 *
 * @author prabhatkrmishra
 * @see SellerLookupService
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SellerLookupServiceImpl implements SellerLookupService {

    private final SellerRepository sellerRepository;

    /**
     * Finds a seller by the associated user ID.
     *
     * @param userId the user ID
     * @return optional containing the seller if found
     */
    @Override
    public Optional<Seller> findByUserId(Long userId) {
        return sellerRepository.findByUserId(userId);
    }

    /**
     * Finds a seller by ID.
     *
     * @param sellerId the seller ID
     * @return optional containing the seller if found
     */
    @Override
    public Optional<Seller> findById(Long sellerId) {
        return sellerRepository.findById(sellerId);
    }

    /**
     * Checks whether a seller exists for the given user ID.
     *
     * @param userId the user ID
     * @return true if a seller exists for the user
     */
    @Override
    public boolean existsByUserId(Long userId) {
        return sellerRepository.existsByUserId(userId);
    }

    /**
     * Checks whether a seller exists with the given business email.
     *
     * @param email the business email
     * @return true if a seller with that email exists
     */
    @Override
    public boolean existsByBusinessEmail(String email) {
        return sellerRepository.existsByBusinessEmail(email);
    }

    /**
     * Finds a paginated list of sellers filtered by verification status.
     *
     * @param status the verification status to filter by
     * @param page   zero-based page index
     * @param size   page size
     * @return paginated seller results
     */
    @Override
    public Page<Seller> findByVerificationStatus(VerificationStatus status, int page, int size) {
        return sellerRepository.findByVerificationStatus(status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }

    /**
     * Retrieves a paginated list of all sellers.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated seller results
     */
    @Override
    public Page<Seller> findAll(int page, int size) {
        return sellerRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }

    /**
     * Returns the total number of seller records.
     *
     * @return total seller count
     */
    @Override
    public long count() {
        return sellerRepository.count();
    }
}
