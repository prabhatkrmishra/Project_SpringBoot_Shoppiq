package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerInventoryResponse;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.general.item.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.general.inventory.ItemStockNegativeException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerSuspendedException;
import com.pkmprojects.shoppiq.service.item.ItemLookupService;
import com.pkmprojects.shoppiq.service.itemdetails.ItemDetailsLookupService;
import com.pkmprojects.shoppiq.service.itemdetails.ItemDetailsWriteService;
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

    private final SellerLookupService sellerLookupService;
    private final ItemLookupService itemLookupService;
    private final ItemDetailsLookupService itemDetailsLookupService;
    private final ItemDetailsWriteService itemDetailsWriteService;

    public SellerInventoryServiceImpl(SellerLookupService sellerLookupService,
                                      ItemLookupService itemLookupService,
                                      ItemDetailsLookupService itemDetailsLookupService,
                                      ItemDetailsWriteService itemDetailsWriteService) {
        this.sellerLookupService = sellerLookupService;
        this.itemLookupService = itemLookupService;
        this.itemDetailsLookupService = itemDetailsLookupService;
        this.itemDetailsWriteService = itemDetailsWriteService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerInventoryResponse> getInventory(User user, int page, int size) {
        Seller seller = findActiveSeller(user);
        var itemPage = itemLookupService.findBySellerId(seller.getId(), page, size);
        return PageResponse.of(itemPage, SellerInventoryResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerInventoryResponse> getLowStockProducts(User user, int page, int size) {
        Seller seller = findActiveSeller(user);
        List<SellerInventoryResponse> content = itemDetailsLookupService
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
        List<SellerInventoryResponse> content = itemDetailsLookupService
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
        Item item = itemLookupService.findByIdAndSellerId(itemId, seller.getId())
                .orElseThrow(() -> ItemNotFoundException.id(itemId));

        ItemDetails details = item.getItemDetails();
        int newQuantity = quantity;

        if (newQuantity < 0) {
            throw ItemStockNegativeException.forAdjustment(details.getStockQuantity(), quantity);
        }

        details.setStockQuantity(newQuantity);
        itemDetailsWriteService.save(details);

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
        Seller seller = sellerLookupService.findByUserId(user.getId())
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
