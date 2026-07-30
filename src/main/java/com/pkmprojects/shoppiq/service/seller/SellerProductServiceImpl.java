package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.item.ItemRequest;
import com.pkmprojects.shoppiq.dto.item.ItemResponse;
import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.business.SlugGenerationFailedException;
import com.pkmprojects.shoppiq.exception.general.category.CategoryNotFoundException;
import com.pkmprojects.shoppiq.exception.general.item.DuplicateItemException;
import com.pkmprojects.shoppiq.exception.general.item.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerSuspendedException;
import com.pkmprojects.shoppiq.service.category.CategoryLookupService;
import com.pkmprojects.shoppiq.service.item.ItemLookupService;
import com.pkmprojects.shoppiq.service.item.ItemWriteService;
import com.pkmprojects.shoppiq.util.SlugUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link SellerProductService} implementation handling seller product lifecycle
 * with precondition enforcement, ownership verification, and SKU uniqueness.
 *
 * @author prabhatkrmishra
 * @see SellerProductService
 * @since 1.0.0
 */
@Service
@Transactional
public class SellerProductServiceImpl implements SellerProductService {

    private final SellerLookupService sellerLookupService;
    private final ItemLookupService itemLookupService;
    private final ItemWriteService itemWriteService;
    private final CategoryLookupService categoryLookupService;

    public SellerProductServiceImpl(SellerLookupService sellerLookupService,
                                    ItemLookupService itemLookupService,
                                    ItemWriteService itemWriteService,
                                    CategoryLookupService categoryLookupService) {
        this.sellerLookupService = sellerLookupService;
        this.itemLookupService = itemLookupService;
        this.itemWriteService = itemWriteService;
        this.categoryLookupService = categoryLookupService;
    }

    /**
     * Creates a new product as DRAFT for the authenticated seller.
     *
     * <p>Validates seller status (ACTIVE, APPROVED, not SUSPENDED), checks
     * SKU uniqueness, resolves the category, generates a unique slug, and
     * persists the item with DRAFT publishing status.</p>
     *
     * @param request product creation payload
     * @param user    authenticated user
     * @return created item response
     * @throws SellerNotFoundException    if no seller exists for the user
     * @throws SellerSuspendedException   if the seller is suspended
     * @throws SellerNotVerifiedException if the seller is not verified
     * @throws DuplicateItemException     if the SKU already exists
     * @throws CategoryNotFoundException  if the category is not found
     */
    @Override
    public ItemResponse createProduct(ItemRequest request, User user) {
        Seller seller = findActiveSeller(user);

        validateSku(request.sku());

        Category category = categoryLookupService.findById(request.categoryId())
                .orElseThrow(() -> CategoryNotFoundException.id(request.categoryId()));

        ItemDetails itemDetails = ItemDetails.builder()
                .brand(request.brand())
                .sku(request.sku())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .discountPercentage(request.discountPercentage())
                .imageUrl(request.imageUrl())
                .category(category)
                .build();

        Item item = Item.builder()
                .name(request.name())
                .slug(generateUniqueSlug(request.name()))
                .description(request.description())
                .seller(seller)
                .publishingStatus(ProductPublishingStatus.DRAFT)
                .itemDetails(itemDetails)
                .build();

        itemDetails.setItem(item);

        return ItemResponse.fromEntity(saveWithSlugRetry(item));
    }

    private Item saveWithSlugRetry(Item item) {
        int attempts = 0;
        while (attempts < 10) {
            try {
                return itemWriteService.save(item);
            } catch (DataIntegrityViolationException e) {
                if (e.getMessage() != null && e.getMessage().contains("slug")) {
                    item.setSlug(generateUniqueSlug(item.getName()));
                    attempts++;
                } else {
                    throw e;
                }
            }
        }
        throw SlugGenerationFailedException.forEntity("item", item.getName(), 10);
    }

    /**
     * Retrieves a paginated list of products owned by the authenticated seller.
     *
     * @param user authenticated user
     * @param page zero-based page index
     * @param size page size
     * @return paginated item responses
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ItemResponse> getMyProducts(User user, int page, int size) {
        Seller seller = findActiveSeller(user);
        var itemPage = itemLookupService.findBySellerId(seller.getId(), page, size);
        return PageResponse.of(itemPage, ItemResponse::fromEntity);
    }

    /**
     * Retrieves a single product by ID, verifying ownership by the authenticated seller.
     *
     * @param id   item ID
     * @param user authenticated user
     * @return item response
     * @throws ItemNotFoundException if the item does not exist or does not belong to the seller
     */
    @Override
    @Transactional(readOnly = true)
    public ItemResponse getMyProductById(Long id, User user) {
        Seller seller = findActiveSeller(user);
        Item item = itemLookupService.findByIdAndSellerId(id, seller.getId())
                .orElseThrow(() -> ItemNotFoundException.id(id));
        return ItemResponse.fromEntity(item);
    }

    /**
     * Updates an existing product with ownership verification.
     *
     * <p>Validates SKU uniqueness, resolves the new category, regenerates
     * the slug if the name changed, and resets publishing status to DRAFT
     * if the product was previously PUBLISHED.</p>
     *
     * @param id      item ID
     * @param request product update payload
     * @param user    authenticated user
     * @return updated item response
     * @throws ItemNotFoundException if the item does not exist or does not belong to the seller
     */
    @Override
    public ItemResponse updateProduct(Long id, ItemRequest request, User user) {
        Seller seller = findActiveSeller(user);
        Item item = itemLookupService.findByIdAndSellerId(id, seller.getId())
                .orElseThrow(() -> ItemNotFoundException.id(id));

        validateSku(request.sku(), id);

        Category category = categoryLookupService.findById(request.categoryId())
                .orElseThrow(() -> CategoryNotFoundException.id(request.categoryId()));

        String originalName = item.getName();
        item.setName(request.name());
        item.setDescription(request.description());

        if (!originalName.equalsIgnoreCase(request.name())) {
            item.setSlug(generateUniqueSlug(request.name()));
        }

        ItemDetails details = item.getItemDetails();
        details.setBrand(request.brand());
        details.setSku(request.sku());
        details.setPrice(request.price());
        details.setStockQuantity(request.stockQuantity());
        details.setDiscountPercentage(request.discountPercentage());
        details.setImageUrl(request.imageUrl());
        details.setCategory(category);

        if (item.getPublishingStatus() == ProductPublishingStatus.PUBLISHED) {
            item.setPublishingStatus(ProductPublishingStatus.DRAFT);
        }

        return ItemResponse.fromEntity(saveWithSlugRetry(item));
    }

    /**
     * Deletes a product with ownership verification.
     *
     * @param id   item ID
     * @param user authenticated user
     * @throws ItemNotFoundException if the item does not exist or does not belong to the seller
     */
    @Override
    public void deleteProduct(Long id, User user) {
        Seller seller = findActiveSeller(user);
        Item item = itemLookupService.findByIdAndSellerId(id, seller.getId())
                .orElseThrow(() -> ItemNotFoundException.id(id));
        itemWriteService.delete(item);
    }

    /**
     * Finds the seller associated with the given user and validates
     * that the seller is in a state that allows product operations.
     *
     * @param user the authenticated user
     * @return the active seller
     */
    private Seller findActiveSeller(User user) {
        Seller seller = sellerLookupService.findByUserId(user.getId())
                .orElseThrow(() -> SellerNotFoundException.userId(user.getId()));

        if (seller.getSellerStatus() == SellerStatus.SUSPENDED) {
            throw SellerSuspendedException.forAction(seller.getId(), "manage products");
        }

        if (seller.getSellerStatus() != SellerStatus.ACTIVE
                || seller.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw SellerNotVerifiedException.forAction(seller.getId(), "manage products");
        }

        return seller;
    }

    /**
     * Validates SKU uniqueness during creation.
     *
     * @param sku SKU to validate
     */
    private void validateSku(String sku) {
        if (itemLookupService.existsByItemDetailsSku(sku)) {
            throw DuplicateItemException.sku(sku);
        }
    }

    /**
     * Validates SKU uniqueness during updates.
     *
     * @param sku SKU to validate
     * @param id  current item id to exclude
     */
    private void validateSku(String sku, Long id) {
        if (itemLookupService.existsByItemDetailsSkuAndIdNot(sku, id)) {
            throw DuplicateItemException.sku(sku);
        }
    }

    /**
     * Generates a unique URL-friendly slug.
     *
     * @param itemName item name
     * @return unique slug
     */
    private String generateUniqueSlug(String itemName) {
        String baseSlug = SlugUtil.toSlug(itemName);
        String slug = baseSlug;
        int counter = 2;

        while (itemLookupService.existsBySlug(slug)) {
            if (counter > 1000) {
                throw SlugGenerationFailedException.forEntity("item", itemName, 1000);
            }
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }
}
