package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.dto.seller.request.SellerProfileUpdateRequest;
import com.pkmprojects.shoppiq.dto.seller.request.SellerRegistrationRequest;
import com.pkmprojects.shoppiq.dto.seller.response.SellerResponse;
import com.pkmprojects.shoppiq.entity.address.Address;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.entity.seller.Store;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.StoreStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.general.seller.SellerAlreadyExistsException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotFoundException;
import com.pkmprojects.shoppiq.repository.address.AddressRepository;
import com.pkmprojects.shoppiq.repository.seller.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * <strong>Spring Boot Concept:</strong> Default implementation of {@link SellerService}.
 *
 * <p><strong>What this Service implementation demonstrates:</strong></p>
 * <ul>
 *   <li><strong>Delegation to specialized services</strong> — Rather than injecting repositories
 *       directly for all operations, this implementation delegates read operations to
 *       {@link SellerLookupService} and write operations to {@link SellerWriteService}.
 *       This follows the single-responsibility principle: this class handles only seller
 *       profile workflows (registration, updates, deactivation, store publishing).</li>
 *   <li><strong>Duplicate detection</strong> — {@link #register} checks both {@code userId}
 *       and {@code businessEmail} for conflicts before creating a seller, using
 *       {@link SellerLookupService} methods.</li>
 *   <li><strong>Soft-delete deactivation</strong> — {@link #deleteProfile} sets
 *       {@code sellerStatus = INACTIVE} rather than deleting the row, preserving history.</li>
 *   <li><strong>Store publishing flow</strong> — {@link #publishStore} looks up the seller,
 *       finds their store, and transitions it to {@code PUBLISHED}, with error handling for
 *       missing or already-published stores.</li>
 * </ul>
 *
 * <p>
 * Handles the seller lifecycle from initial registration to profile updates.
 * Registration sets {@code verificationStatus = PENDING} and
 * {@code sellerStatus = INACTIVE}. Activation occurs via admin approval
 * (Phase 15.3).
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Service
@Transactional
public class SellerServiceImpl implements SellerService {

    private final SellerLookupService sellerLookupService;
    private final SellerWriteService sellerWriteService;
    private final AddressRepository addressRepository;
    private final StoreRepository storeRepository;
    private final Clock clock;

    public SellerServiceImpl(SellerLookupService sellerLookupService,
                             SellerWriteService sellerWriteService,
                             AddressRepository addressRepository,
                             StoreRepository storeRepository,
                             Clock clock) {
        this.sellerLookupService = sellerLookupService;
        this.sellerWriteService = sellerWriteService;
        this.addressRepository = addressRepository;
        this.storeRepository = storeRepository;
        this.clock = clock;
    }

    /**
     * Registers a new seller with duplicate detection for userId and businessEmail.
     *
     * <p>Sets verification status to PENDING and seller status to INACTIVE.
     * Activation occurs via admin approval.</p>
     *
     * @param request seller registration payload
     * @param user    authenticated user
     * @return created seller response
     * @throws SellerAlreadyExistsException if the user or business email is already registered
     */
    @Override
    public SellerResponse register(SellerRegistrationRequest request, User user) {
        if (sellerLookupService.existsByUserId(user.getId())) {
            throw SellerAlreadyExistsException.forUser(user.getId());
        }

        if (sellerLookupService.existsByBusinessEmail(request.businessEmail())) {
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
                .joinedAt(Instant.now(clock))
                .build();

        seller = sellerWriteService.save(seller);
        return SellerResponse.fromEntity(seller);
    }

    /**
     * Retrieves the seller profile for the authenticated user.
     *
     * @param user authenticated user
     * @return seller profile response
     * @throws SellerNotFoundException if no seller exists for the user
     */
    @Override
    @Transactional(readOnly = true)
    public SellerResponse getProfile(User user) {
        Seller seller = sellerLookupService.findByUserId(user.getId())
                .orElseThrow(() -> SellerNotFoundException.userId(user.getId()));
        return SellerResponse.fromEntity(seller);
    }

    /**
     * Updates the seller profile with partial field-level granularity.
     *
     * <p>Only non-null fields from the request are applied. If a business
     * address is provided, a new address record is created and associated.</p>
     *
     * @param request profile update payload
     * @param user    authenticated user
     * @return updated seller response
     * @throws SellerNotFoundException if no seller exists for the user
     */
    @Override
    public SellerResponse updateProfile(SellerProfileUpdateRequest request, User user) {
        Seller seller = sellerLookupService.findByUserId(user.getId())
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

        seller = sellerWriteService.save(seller);
        return SellerResponse.fromEntity(seller);
    }

    /**
     * Soft-deletes the seller profile by setting status to INACTIVE.
     *
     * @param user authenticated user
     * @throws SellerNotFoundException if no seller exists for the user
     */
    @Override
    public void deleteProfile(User user) {
        Seller seller = sellerLookupService.findByUserId(user.getId())
                .orElseThrow(() -> SellerNotFoundException.userId(user.getId()));

        seller.setSellerStatus(SellerStatus.INACTIVE);
        sellerWriteService.save(seller);
    }

    /**
     * Publishes the seller's store, transitioning it to PUBLISHED status.
     *
     * @param user authenticated user
     * @throws SellerNotFoundException if no seller exists for the user
     * @throws IllegalStateException   if the store is already published or not found
     */
    @Override
    public void publishStore(User user) {
        Seller seller = sellerLookupService.findByUserId(user.getId())
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
