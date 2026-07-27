package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminSellerResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.role.Role;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.entity.seller.Store;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.StoreStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.general.seller.SellerApprovalInvalidException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotFoundException;
import com.pkmprojects.shoppiq.repository.seller.StoreRepository;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import com.pkmprojects.shoppiq.service.role.RoleService;
import com.pkmprojects.shoppiq.service.seller.SellerLookupService;
import com.pkmprojects.shoppiq.service.seller.SellerWriteService;
import com.pkmprojects.shoppiq.util.SlugUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link AdminSellerService}.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminSellerServiceImpl implements AdminSellerService {

    private final SellerLookupService sellerLookupService;
    private final SellerWriteService sellerWriteService;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final RoleService roleService;

    public AdminSellerServiceImpl(SellerLookupService sellerLookupService,
                                  SellerWriteService sellerWriteService,
                                  StoreRepository storeRepository,
                                  UserRepository userRepository,
                                   RoleService roleService) {
        this.sellerLookupService = sellerLookupService;
        this.sellerWriteService = sellerWriteService;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminSellerResponse> getAllSellers(int page, int size) {
        var sellerPage = sellerLookupService.findAll(page, size);
        return PageResponse.of(sellerPage, AdminSellerResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminSellerResponse> getSellersByStatus(VerificationStatus status, int page, int size) {
        var sellerPage = sellerLookupService.findByVerificationStatus(status, page, size);
        return PageResponse.of(sellerPage, AdminSellerResponse::fromEntity);
    }

    @Override
    public AdminSellerResponse approveSeller(Long sellerId) {
        Seller seller = sellerLookupService.findById(sellerId)
                .orElseThrow(() -> SellerNotFoundException.id(sellerId));

        if (seller.getVerificationStatus() != VerificationStatus.PENDING) {
            throw SellerApprovalInvalidException.notPending(sellerId);
        }

        seller.setVerificationStatus(VerificationStatus.APPROVED);
        seller.setSellerStatus(SellerStatus.ACTIVE);
        seller = sellerWriteService.save(seller);

        createStore(seller);
        grantSellerRole(seller.getUser());

        return AdminSellerResponse.fromEntity(seller);
    }

    @Override
    public AdminSellerResponse rejectSeller(Long sellerId) {
        Seller seller = sellerLookupService.findById(sellerId)
                .orElseThrow(() -> SellerNotFoundException.id(sellerId));

        if (seller.getVerificationStatus() != VerificationStatus.PENDING) {
            throw SellerApprovalInvalidException.notPending(sellerId);
        }

        seller.setVerificationStatus(VerificationStatus.REJECTED);
        seller.setSellerStatus(SellerStatus.INACTIVE);
        seller = sellerWriteService.save(seller);

        return AdminSellerResponse.fromEntity(seller);
    }

    @Override
    public AdminSellerResponse suspendSeller(Long sellerId) {
        Seller seller = sellerLookupService.findById(sellerId)
                .orElseThrow(() -> SellerNotFoundException.id(sellerId));

        if (seller.getSellerStatus() != SellerStatus.ACTIVE) {
            throw SellerApprovalInvalidException.notActive(sellerId);
        }

        seller.setSellerStatus(SellerStatus.SUSPENDED);
        seller = sellerWriteService.save(seller);

        storeRepository.findBySellerId(sellerId).ifPresent(store -> {
            store.setStatus(StoreStatus.SUSPENDED);
            storeRepository.save(store);
        });

        return AdminSellerResponse.fromEntity(seller);
    }

    @Override
    public AdminSellerResponse unsuspendSeller(Long sellerId) {
        Seller seller = sellerLookupService.findById(sellerId)
                .orElseThrow(() -> SellerNotFoundException.id(sellerId));

        if (seller.getSellerStatus() != SellerStatus.SUSPENDED) {
            throw SellerApprovalInvalidException.notSuspended(sellerId);
        }

        seller.setSellerStatus(SellerStatus.ACTIVE);
        seller = sellerWriteService.save(seller);

        storeRepository.findBySellerId(sellerId).ifPresent(store -> {
            store.setStatus(StoreStatus.DRAFT);
            storeRepository.save(store);
        });

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
        Role sellerRole = roleService.getSellerRole();
        user.addRole(sellerRole);
        userRepository.save(user);
    }
}
