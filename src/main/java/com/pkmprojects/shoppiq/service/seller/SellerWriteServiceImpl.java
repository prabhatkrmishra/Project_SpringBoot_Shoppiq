package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.repository.seller.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link SellerWriteService} implementation providing transactional persistence
 * for seller entities.
 *
 * @author prabhatkrmishra
 * @see SellerWriteService
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
class SellerWriteServiceImpl implements SellerWriteService {

    private final SellerRepository sellerRepository;

    /**
     * Persists the given seller entity.
     *
     * @param seller the seller entity to save
     * @return the saved seller entity
     */
    @Override
    @Transactional
    public Seller save(Seller seller) {
        return sellerRepository.save(seller);
    }

    /**
     * Deletes a seller by ID.
     *
     * @param sellerId the seller ID
     */
    @Override
    @Transactional
    public void deleteById(Long sellerId) {
        sellerRepository.deleteById(sellerId);
    }
}
