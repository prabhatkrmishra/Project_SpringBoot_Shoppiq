package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.repository.seller.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link SellerWriteService}.
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
class SellerWriteServiceImpl implements SellerWriteService {

    private final SellerRepository sellerRepository;

    @Override
    @Transactional
    public Seller save(Seller seller) {
        return sellerRepository.save(seller);
    }

    @Override
    @Transactional
    public void deleteById(Long sellerId) {
        sellerRepository.deleteById(sellerId);
    }
}
