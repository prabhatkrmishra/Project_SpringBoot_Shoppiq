package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.admin.response.AdminSellerResponse;
import com.pkmprojects.shoppiq.entity.Role;
import com.pkmprojects.shoppiq.entity.Seller;
import com.pkmprojects.shoppiq.entity.Store;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.StoreStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.SellerApprovalInvalidException;
import com.pkmprojects.shoppiq.exception.SellerNotFoundException;
import com.pkmprojects.shoppiq.repository.SellerRepository;
import com.pkmprojects.shoppiq.repository.StoreRepository;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.service.admin.AdminSellerService;
import com.pkmprojects.shoppiq.util.SlugUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of {@link AdminSellerService}.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminSellerServiceImpl implements AdminSellerService {

    private final SellerRepository sellerRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final RolesService rolesService;

    public AdminSellerServiceImpl(SellerRepository sellerRepository,
                                  StoreRepository storeRepository,
                                  UserRepository userRepository,
                                  RolesService rolesService) {
        this.sellerRepository = sellerRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.rolesService = rolesService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminSellerResponse> getAllSellers() {
        return sellerRepository.findAll().stream()
                .map(AdminSellerResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminSellerResponse> getSellersByStatus(VerificationStatus status) {
        return sellerRepository.findByVerificationStatus(status).stream()
                .map(AdminSellerResponse::fromEntity)
                .toList();
    }

    @Override
    public AdminSellerResponse approveSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> SellerNotFoundException.id(sellerId));

        if (seller.getVerificationStatus() != VerificationStatus.PENDING) {
            throw SellerApprovalInvalidException.notPending(sellerId);
        }

        seller.setVerificationStatus(VerificationStatus.APPROVED);
        seller.setSellerStatus(SellerStatus.ACTIVE);
        seller = sellerRepository.save(seller);

        createStore(seller);
        grantSellerRole(seller.getUser());

        return AdminSellerResponse.fromEntity(seller);
    }

    @Override
    public AdminSellerResponse rejectSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> SellerNotFoundException.id(sellerId));

        if (seller.getVerificationStatus() != VerificationStatus.PENDING) {
            throw SellerApprovalInvalidException.notPending(sellerId);
        }

        seller.setVerificationStatus(VerificationStatus.REJECTED);
        seller.setSellerStatus(SellerStatus.INACTIVE);
        seller = sellerRepository.save(seller);

        return AdminSellerResponse.fromEntity(seller);
    }

    private void createStore(Seller seller) {
        String baseSlug = SlugUtil.toSlug(seller.getBusinessName());
        String slug = baseSlug;

        int suffix = 1;
        while (storeRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + suffix++;
        }

        Store store = Store.builder()
                .seller(seller)
                .storeName(seller.getBusinessName())
                .slug(slug)
                .status(StoreStatus.DRAFT)
                .build();

        storeRepository.save(store);
    }

    private void grantSellerRole(User user) {
        Role sellerRole = rolesService.getSellerRole();
        user.addRole(sellerRole);
        userRepository.save(user);
    }
}
