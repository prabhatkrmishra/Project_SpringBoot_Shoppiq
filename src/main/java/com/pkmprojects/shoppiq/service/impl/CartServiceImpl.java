package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.request.AddCartItemRequest;
import com.pkmprojects.shoppiq.dto.response.CartItemResponse;
import com.pkmprojects.shoppiq.dto.response.CartResponse;
import com.pkmprojects.shoppiq.dto.request.UpdateCartItemRequest;
import com.pkmprojects.shoppiq.entity.Cart;
import com.pkmprojects.shoppiq.entity.CartItem;
import com.pkmprojects.shoppiq.entity.ItemDetails;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.CartItemAccessDeniedException;
import com.pkmprojects.shoppiq.exception.CartItemNotFoundException;
import com.pkmprojects.shoppiq.exception.InsufficientStockException;
import com.pkmprojects.shoppiq.exception.ItemDetailsNotFoundException;
import com.pkmprojects.shoppiq.repository.CartItemRepository;
import com.pkmprojects.shoppiq.repository.CartRepository;
import com.pkmprojects.shoppiq.repository.ItemDetailsRepository;
import com.pkmprojects.shoppiq.service.CartService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Default implementation of {@link CartService}.
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Uses constructor injection.</li>
 *     <li>Works exclusively with DTOs at the controller boundary.</li>
 *     <li>All write operations run inside transactions.</li>
 *     <li>Effective price = price × (1 - discountPercentage / 100),
 *         rounded to 2 decimal places using HALF_UP.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemDetailsRepository itemDetailsRepository;

    public CartServiceImpl(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ItemDetailsRepository itemDetailsRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.itemDetailsRepository = itemDetailsRepository;
    }

    // ---------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------

    @Override
    public CartItemResponse create(User user, AddCartItemRequest request) {

        Cart cart = findOrCreateCart(user);

        ItemDetails itemDetails = itemDetailsRepository
                .findById(request.itemDetailsId())
                .orElseThrow(() -> new ItemDetailsNotFoundException(
                        "Item details with id '%d' were not found.".formatted(request.itemDetailsId())
                ));

        validateStock(itemDetails, request.quantity());

        CartItem cartItem = cartItemRepository
                .findByCartAndItemDetails(cart, itemDetails)
                .map(existing -> increaseQuantity(existing, request.quantity(), itemDetails))
                .orElseGet(() -> createNewCartItem(cart, itemDetails, request.quantity()));

        cartItemRepository.save(cartItem);
        return toCartItemResponse(cartItem);
    }

    @Override
    public CartResponse get(User user) {

        return cartRepository.findByUser(user)
                .map(cart -> {
                    List<CartItem> items = cartItemRepository.findAllByCart(cart);
                    List<CartItemResponse> itemResponses = items.stream()
                            .map(this::toCartItemResponse)
                            .toList();
                    BigDecimal subtotal = calculateSubtotal(items);
                    return new CartResponse(cart.getId(), items.size(), subtotal, itemResponses);
                })
                .orElseGet(() -> new CartResponse(null, 0, BigDecimal.ZERO, List.of()));
    }

    @Override
    public CartItemResponse getById(User user, Long cartItemId) {
        CartItem cartItem = resolveOwnedCartItem(user, cartItemId);
        return toCartItemResponse(cartItem);
    }

    @Override
    public CartItemResponse update(User user, Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = resolveOwnedCartItem(user, cartItemId);
        validateStock(cartItem.getItemDetails(), request.quantity());
        cartItem.setQuantity(request.quantity());
        cartItemRepository.save(cartItem);
        return toCartItemResponse(cartItem);
    }

    @Override
    public void delete(User user, Long cartItemId) {
        CartItem cartItem = resolveOwnedCartItem(user, cartItemId);
        cartItemRepository.delete(cartItem);
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    /**
     * Returns the user's cart, creating one if it does not yet exist.
     */
    private Cart findOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(user).build()
                ));
    }

    /**
     * Creates a new cart item for the given product.
     */
    private CartItem createNewCartItem(Cart cart, ItemDetails itemDetails, int quantity) {
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .itemDetails(itemDetails)
                .quantity(quantity)
                .build();
        cart.addItem(cartItem);
        return cartItem;
    }

    /**
     * Increases the quantity of an existing cart item, validating stock
     * against the new total.
     */
    private CartItem increaseQuantity(CartItem existing, int additionalQty, ItemDetails itemDetails) {
        int newQty = existing.getQuantity() + additionalQty;
        validateStock(itemDetails, newQty);
        existing.setQuantity(newQty);
        return existing;
    }

    /**
     * Finds a cart item by ID and verifies it belongs to the user's cart.
     *
     * @throws CartItemNotFoundException     when the item ID does not exist
     * @throws CartItemAccessDeniedException when the item belongs to another cart
     */
    private CartItem resolveOwnedCartItem(User user, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> CartItemNotFoundException.id(cartItemId));

        Cart userCart = cartRepository.findByUser(user)
                .orElseThrow(() -> CartItemNotFoundException.id(cartItemId));

        if (cartItem.getCart() == null
                || !cartItem.getCart().getId().equals(userCart.getId())) {
            throw CartItemAccessDeniedException.forItem(cartItemId);
        }

        return cartItem;
    }

    /**
     * Validates that the requested quantity does not exceed available stock.
     *
     * @throws InsufficientStockException when stock is insufficient
     */
    private void validateStock(ItemDetails itemDetails, int requestedQty) {
        int available = itemDetails.getStockQuantity();
        if (requestedQty > available) {
            throw InsufficientStockException.forItem(
                    itemDetails.getSku(), requestedQty, available
            );
        }
    }

    /**
     * Computes the total cart subtotal.
     *
     * <p>Line total = effectivePrice × quantity per item.</p>
     */
    private BigDecimal calculateSubtotal(List<CartItem> items) {
        return items.stream()
                .map(item -> effectivePrice(item.getItemDetails())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Computes the effective (post-discount) price for an item.
     */
    private BigDecimal effectivePrice(ItemDetails itemDetails) {
        BigDecimal discount = itemDetails.getDiscountPercentage()
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return itemDetails.getPrice()
                .multiply(BigDecimal.ONE.subtract(discount))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Maps a {@link CartItem} to a {@link CartItemResponse}.
     *
     * <p>Accesses the related {@link com.pkmprojects.shoppiq.entity.Item}
     * via the inverse side of the ItemDetails → Item relationship.</p>
     */
    private CartItemResponse toCartItemResponse(CartItem cartItem) {
        ItemDetails details = cartItem.getItemDetails();
        BigDecimal unitPrice = effectivePrice(details);
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        String itemName = details.getItem() != null ? details.getItem().getName() : "";
        Long itemId = details.getItem() != null ? details.getItem().getId() : null;
        String itemSlug = details.getItem() != null ? details.getItem().getSlug() : "";

        return new CartItemResponse(
                cartItem.getId(),
                details.getId(),
                itemId,
                itemSlug,
                itemName,
                details.getBrand(),
                details.getSku(),
                unitPrice,
                details.getPrice(),
                details.getDiscountPercentage(),
                cartItem.getQuantity(),
                lineTotal,
                details.getImageUrl()
        );
    }
}
