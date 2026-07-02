package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.seller.request.SellerProfileUpdateRequest;
import com.pkmprojects.shoppiq.dto.seller.request.SellerRegistrationRequest;
import com.pkmprojects.shoppiq.dto.seller.response.SellerResponse;
import com.pkmprojects.shoppiq.entity.Address;
import com.pkmprojects.shoppiq.entity.Seller;
import com.pkmprojects.shoppiq.entity.Store;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.StoreStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.SellerAlreadyExistsException;
import com.pkmprojects.shoppiq.exception.SellerNotFoundException;
import com.pkmprojects.shoppiq.repository.AddressRepository;
import com.pkmprojects.shoppiq.repository.SellerRepository;
import com.pkmprojects.shoppiq.repository.StoreRepository;
import com.pkmprojects.shoppiq.service.seller.SellerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Default implementation of {@link SellerService}.
 *
 * <p>
 * Handles the seller lifecycle from initial registration to profile updates.
 * Registration sets {@code verificationStatus = PENDING} and
 * {@code sellerStatus = INACTIVE}. Activation occurs via admin approval
 * (Phase 15.3).
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final AddressRepository addressRepository;
    private final StoreRepository storeRepository;

    public SellerServiceImpl(SellerRepository sellerRepository,
                             AddressRepository addressRepository,
                             StoreRepository storeRepository) {
        this.sellerRepository = sellerRepository;
        this.addressRepository = addressRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public SellerResponse register(SellerRegistrationRequest request, User user) {
        if (sellerRepository.existsByUserId(user.getId())) {
            throw SellerAlreadyExistsException.forUser(user.getId());
        }

        if (sellerRepository.existsByBusinessEmail(request.businessEmail())) {
            throw SellerAlreadyExistsException.forEmail(request.businessEmail());
        }

        Seller seller = Seller.builder()
                .user(user)
                .businessName(request.businessName())
                .businessEmail(request.businessEmail())
                .phone(request.phone())
                .gstNumber(request.gstNumber())
                .panNumber(request.panNumber())
                .verificationStatus(VerificationStatus.PENDING)
                .sellerStatus(SellerStatus.INACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        seller = sellerRepository.save(seller);
        return SellerResponse.fromEntity(seller);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerResponse getProfile(User user) {
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> SellerNotFoundException.userId(user.getId()));
        return SellerResponse.fromEntity(seller);
    }

    @Override
    public SellerResponse updateProfile(SellerProfileUpdateRequest request, User user) {
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> SellerNotFoundException.userId(user.getId()));

        if (request.businessName() != null) {
            seller.setBusinessName(request.businessName());
        }
        if (request.businessEmail() != null) {
            seller.setBusinessEmail(request.businessEmail());
        }
        if (request.phone() != null) {
            seller.setPhone(request.phone());
        }
        if (request.gstNumber() != null) {
            seller.setGstNumber(request.gstNumber());
        }
        if (request.panNumber() != null) {
            seller.setPanNumber(request.panNumber());
        }
        if (request.businessAddress() != null) {
            var addrReq = request.businessAddress();
            Address address = Address.builder()
                    .user(user)
                    .label(addrReq.label())
                    .fullName(addrReq.fullName())
                    .phone(addrReq.phone())
                    .line1(addrReq.line1())
                    .line2(addrReq.line2())
                    .city(addrReq.city())
                    .state(addrReq.state())
                    .postalCode(addrReq.postalCode())
                    .country(addrReq.country())
                    .build();
            address = addressRepository.save(address);
            seller.setBusinessAddress(address);
        }

        seller = sellerRepository.save(seller);
        return SellerResponse.fromEntity(seller);
    }

    @Override
    public void deleteProfile(User user) {
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> SellerNotFoundException.userId(user.getId()));

        seller.setSellerStatus(SellerStatus.INACTIVE);
        sellerRepository.save(seller);
    }

    @Override
    public void publishStore(User user) {
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> SellerNotFoundException.userId(user.getId()));

        Store store = storeRepository.findBySellerId(seller.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "No store found for seller '%d'.".formatted(seller.getId())));

        if (store.getStatus() == StoreStatus.PUBLISHED) {
            throw new IllegalStateException("Store is already published.");
        }

        store.setStatus(StoreStatus.PUBLISHED);
        storeRepository.save(store);
    }
}
