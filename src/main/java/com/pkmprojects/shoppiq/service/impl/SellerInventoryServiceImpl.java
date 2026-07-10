package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerInventoryResponse;
import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.entity.ItemDetails;
import com.pkmprojects.shoppiq.entity.Seller;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.ItemStockNegativeException;
import com.pkmprojects.shoppiq.exception.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.SellerSuspendedException;
import com.pkmprojects.shoppiq.repository.ItemDetailsRepository;
import com.pkmprojects.shoppiq.repository.ItemRepository;
import com.pkmprojects.shoppiq.repository.SellerRepository;
import com.pkmprojects.shoppiq.service.seller.SellerInventoryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of {@link SellerInventoryService}.
 *
 * <p>
 * Provides inventory management for sellers. Enforces seller preconditions
 * (ACTIVE, APPROVED, not SUSPENDED) and ownership verification before
 * allowing stock operations.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class SellerInventoryServiceImpl implements SellerInventoryService {

    private final SellerRepository sellerRepository;
    private final ItemRepository itemRepository;
    private final ItemDetailsRepository itemDetailsRepository;

    public SellerInventoryServiceImpl(SellerRepository sellerRepository,
                                      ItemRepository itemRepository,
                                      ItemDetailsRepository itemDetailsRepository) {
        this.sellerRepository = sellerRepository;
        this.itemRepository = itemRepository;
        this.itemDetailsRepository = itemDetailsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerInventoryResponse> getInventory(User user, int page, int size) {
        Seller seller = findActiveSeller(user);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var itemPage = itemRepository.findBySellerId(seller.getId(), pageable);
        return PageResponse.of(itemPage, SellerInventoryResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerInventoryResponse> getLowStockProducts(User user, int page, int size) {
        Seller seller = findActiveSeller(user);
        List<SellerInventoryResponse> content = itemDetailsRepository
                .findLowStockProductsBySellerId(LOW_STOCK_THRESHOLD, seller.getId())
                .stream()
                .map(ItemDetails::getItem)
                .map(SellerInventoryResponse::from)
                .toList();
        return new PageResponse<>(content, 0, content.size(), content.size(), 1, true, true);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerInventoryResponse> getOutOfStockProducts(User user, int page, int size) {
        Seller seller = findActiveSeller(user);
        List<SellerInventoryResponse> content = itemDetailsRepository
                .findOutOfStockProductsBySellerId(seller.getId())
                .stream()
                .map(ItemDetails::getItem)
                .map(SellerInventoryResponse::from)
                .toList();
        return new PageResponse<>(content, 0, content.size(), content.size(), 1, true, true);
    }

    @Override
    public SellerInventoryResponse adjustStock(Long itemId, int quantity, String reason, User user) {
        Seller seller = findActiveSeller(user);
        Item item = itemRepository.findByIdAndSellerId(itemId, seller.getId())
                .orElseThrow(() -> ItemNotFoundException.id(itemId));

        ItemDetails details = item.getItemDetails();
        int newQuantity = quantity;

        if (newQuantity < 0) {
            throw ItemStockNegativeException.forAdjustment(details.getStockQuantity(), quantity);
        }

        details.setStockQuantity(newQuantity);
        itemDetailsRepository.save(details);

        return SellerInventoryResponse.from(item);
    }

    private static final int LOW_STOCK_THRESHOLD = 5;

    /**
     * Finds the seller associated with the given user and validates
     * that the seller is in a state that allows inventory operations.
     *
     * @param user the authenticated user
     * @return the active seller
     */
    private Seller findActiveSeller(User user) {
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> SellerNotFoundException.userId(user.getId()));

        if (seller.getSellerStatus() == SellerStatus.SUSPENDED) {
            throw SellerSuspendedException.forAction(seller.getId(), "manage inventory");
        }

        if (seller.getSellerStatus() != SellerStatus.ACTIVE
                || seller.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw SellerNotVerifiedException.forAction(seller.getId(), "manage inventory");
        }

        return seller;
    }
}
