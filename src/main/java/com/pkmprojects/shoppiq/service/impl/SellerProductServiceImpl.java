package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.request.ItemRequest;
import com.pkmprojects.shoppiq.dto.response.ItemResponse;
import com.pkmprojects.shoppiq.entity.Category;
import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.entity.ItemDetails;
import com.pkmprojects.shoppiq.entity.Seller;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.CategoryNotFoundException;
import com.pkmprojects.shoppiq.exception.DuplicateItemException;
import com.pkmprojects.shoppiq.exception.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.SellerSuspendedException;
import com.pkmprojects.shoppiq.repository.CategoryRepository;
import com.pkmprojects.shoppiq.repository.ItemRepository;
import com.pkmprojects.shoppiq.repository.SellerRepository;
import com.pkmprojects.shoppiq.service.seller.SellerProductService;
import com.pkmprojects.shoppiq.util.SlugUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of {@link SellerProductService}.
 *
 * <p>
 * Handles the lifecycle of products owned by a seller. Enforces seller
 * preconditions (ACTIVE, APPROVED, not SUSPENDED) before allowing
 * product operations. All products are created as DRAFT.
 * </p>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Ownership is verified at the service layer via
 *     {@link ItemRepository#findByIdAndSellerId}.</li>
 *     <li>SKU uniqueness is enforced across the entire catalog,
 *     not per seller.</li>
 *     <li>All write operations are transactional.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class SellerProductServiceImpl implements SellerProductService {

    private final SellerRepository sellerRepository;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    public SellerProductServiceImpl(SellerRepository sellerRepository,
                                    ItemRepository itemRepository,
                                    CategoryRepository categoryRepository) {
        this.sellerRepository = sellerRepository;
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ItemResponse createProduct(ItemRequest request, User user) {
        Seller seller = findActiveSeller(user);

        validateSku(request.sku());

        Category category = categoryRepository.findById(request.categoryId())
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

        return ItemResponse.fromEntity(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getMyProducts(User user) {
        Seller seller = findActiveSeller(user);
        return itemRepository.findBySellerId(seller.getId())
                .stream()
                .map(ItemResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getMyProductById(Long id, User user) {
        Seller seller = findActiveSeller(user);
        Item item = itemRepository.findByIdAndSellerId(id, seller.getId())
                .orElseThrow(() -> ItemNotFoundException.id(id));
        return ItemResponse.fromEntity(item);
    }

    @Override
    public ItemResponse updateProduct(Long id, ItemRequest request, User user) {
        Seller seller = findActiveSeller(user);
        Item item = itemRepository.findByIdAndSellerId(id, seller.getId())
                .orElseThrow(() -> ItemNotFoundException.id(id));

        validateSku(request.sku(), id);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> CategoryNotFoundException.id(request.categoryId()));

        item.setName(request.name());
        item.setDescription(request.description());

        if (!item.getName().equalsIgnoreCase(request.name())) {
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

        return ItemResponse.fromEntity(itemRepository.save(item));
    }

    @Override
    public void deleteProduct(Long id, User user) {
        Seller seller = findActiveSeller(user);
        Item item = itemRepository.findByIdAndSellerId(id, seller.getId())
                .orElseThrow(() -> ItemNotFoundException.id(id));
        itemRepository.delete(item);
    }

    /**
     * Finds the seller associated with the given user and validates
     * that the seller is in a state that allows product operations.
     *
     * @param user the authenticated user
     * @return the active seller
     */
    private Seller findActiveSeller(User user) {
        Seller seller = sellerRepository.findByUserId(user.getId())
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
        if (itemRepository.existsByItemDetailsSku(sku)) {
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
        if (itemRepository.existsByItemDetailsSkuAndIdNot(sku, id)) {
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

        while (itemRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }
}
