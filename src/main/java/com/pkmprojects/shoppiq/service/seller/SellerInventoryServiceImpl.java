package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.config.InventoryConstants;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerInventoryResponse;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.general.inventory.ItemStockNegativeException;
import com.pkmprojects.shoppiq.exception.general.item.ItemNotFoundException;
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
 * {@link SellerInventoryService} implementation providing inventory management for sellers
 * with seller precondition enforcement and ownership verification.
 *
 * @author prabhatkrmishra
 * @see SellerInventoryService
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

    /**
     * Retrieves a paginated list of the seller's inventory.
     *
     * @param user authenticated user
     * @param page zero-based page index
     * @param size page size
     * @return paginated inventory responses
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerInventoryResponse> getInventory(User user, int page, int size) {
        Seller seller = findActiveSeller(user);
        var itemPage = itemLookupService.findBySellerId(seller.getId(), page, size);
        return PageResponse.of(itemPage, SellerInventoryResponse::from);
    }

    /**
     * Retrieves products with stock below the low-stock threshold for the seller.
     *
     * @param user authenticated user
     * @param page zero-based page index (unused, all results returned as single page)
     * @param size page size (unused)
     * @return paginated low-stock inventory responses
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerInventoryResponse> getLowStockProducts(User user, int page, int size) {
        Seller seller = findActiveSeller(user);
        List<SellerInventoryResponse> content = itemDetailsLookupService
                .findLowStockProductsBySellerId(InventoryConstants.LOW_STOCK_THRESHOLD, seller.getId())
                .stream()
                .map(ItemDetails::getItem)
                .map(SellerInventoryResponse::from)
                .toList();
        return new PageResponse<>(content, 0, content.size(), content.size(), 1, true, true);
    }

    /**
     * Retrieves products with zero stock for the seller.
     *
     * @param user authenticated user
     * @param page zero-based page index (unused, all results returned as single page)
     * @param size page size (unused)
     * @return paginated out-of-stock inventory responses
     */
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

    /**
     * Adjusts the stock quantity for a seller's product with ownership verification.
     *
     * <p>The adjustment value can be positive (add stock) or negative (remove stock).
     * Validates that the resulting quantity is non-negative.</p>
     *
     * @param itemId   item ID
     * @param quantity quantity adjustment (positive or negative)
     * @param reason   reason for the adjustment
     * @param user     authenticated user
     * @return updated inventory response
     * @throws ItemNotFoundException      if the item does not exist or does not belong to the seller
     * @throws ItemStockNegativeException if the adjustment would result in negative stock
     */
    @Override
    public SellerInventoryResponse adjustStock(Long itemId, int quantity, String reason, User user) {
        Seller seller = findActiveSeller(user);
        Item item = itemLookupService.findByIdAndSellerId(itemId, seller.getId())
                .orElseThrow(() -> ItemNotFoundException.id(itemId));

        ItemDetails details = item.getItemDetails();
        int currentQuantity = details.getStockQuantity();
        int newQuantity = currentQuantity + quantity;

        if (newQuantity < 0) {
            throw ItemStockNegativeException.forAdjustment(currentQuantity, quantity);
        }

        details.setStockQuantity(newQuantity);
        itemDetailsWriteService.save(details);

        return SellerInventoryResponse.from(item);
    }

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
