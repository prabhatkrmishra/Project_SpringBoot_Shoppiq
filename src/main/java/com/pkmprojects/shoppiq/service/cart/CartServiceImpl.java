package com.pkmprojects.shoppiq.service.cart;

import com.pkmprojects.shoppiq.dto.cart.AddCartItemRequest;
import com.pkmprojects.shoppiq.dto.cart.CartItemResponse;
import com.pkmprojects.shoppiq.dto.cart.CartResponse;
import com.pkmprojects.shoppiq.dto.cart.UpdateCartItemRequest;
import com.pkmprojects.shoppiq.entity.cart.Cart;
import com.pkmprojects.shoppiq.entity.cart.CartItem;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.exception.general.cart.CartItemAccessDeniedException;
import com.pkmprojects.shoppiq.exception.general.cart.CartItemNotFoundException;
import com.pkmprojects.shoppiq.exception.general.inventory.InsufficientStockException;
import com.pkmprojects.shoppiq.exception.general.item.ItemDetailsNotFoundException;
import com.pkmprojects.shoppiq.repository.cart.CartItemRepository;
import com.pkmprojects.shoppiq.repository.cart.CartRepository;
import com.pkmprojects.shoppiq.service.itemdetails.ItemDetailsLookupService;
import com.pkmprojects.shoppiq.util.PriceUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * {@link CartService} implementation that manages the user's cart: adding items
 * with stock validation, retrieving with calculated subtotals, updating quantities,
 * deleting items with ownership verification, and clearing the cart.
 *
 * @author prabhatkrmishra
 * @see CartService
 * @since 1.0.0
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemDetailsLookupService itemDetailsLookupService;

    public CartServiceImpl(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ItemDetailsLookupService itemDetailsLookupService
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.itemDetailsLookupService = itemDetailsLookupService;
    }

    // ---------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------

    /**
     * Adds an item to the user's cart, creating the cart if it does not exist.
     *
     * <p>Validates stock availability and merges quantity if the item already exists in the cart.</p>
     *
     * @param user    authenticated user
     * @param request add-to-cart payload
     * @return created or updated cart item response
     * @throws ItemDetailsNotFoundException if the item details do not exist
     * @throws InsufficientStockException   if stock is insufficient
     */
    @Override
    public CartItemResponse create(User user, AddCartItemRequest request) {

        Cart cart = findOrCreateCart(user);

        ItemDetails itemDetails = itemDetailsLookupService
                .findById(request.itemDetailsId())
                .orElseThrow(() -> ItemDetailsNotFoundException.id(request.itemDetailsId()));

        validateStock(itemDetails, request.quantity());

        CartItem cartItem = cartItemRepository
                .findByCartAndItemDetails(cart, itemDetails)
                .map(existing -> increaseQuantity(existing, request.quantity(), itemDetails))
                .orElseGet(() -> createNewCartItem(cart, itemDetails, request.quantity()));

        cartItemRepository.save(cartItem);
        return toCartItemResponse(cartItem);
    }

    /**
     * Retrieves the current user's cart with calculated subtotals.
     *
     * @param user authenticated user
     * @return cart response with items and subtotal, or empty cart if none exists
     */
    @Override
    @Transactional(readOnly = true)
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

    /**
     * Retrieves a specific cart item by ID with ownership verification.
     *
     * @param user       authenticated user
     * @param cartItemId cart item ID
     * @return cart item response
     * @throws CartItemNotFoundException     if the item does not exist
     * @throws CartItemAccessDeniedException if the item belongs to another user
     */
    @Override
    @Transactional(readOnly = true)
    public CartItemResponse getById(User user, Long cartItemId) {
        CartItem cartItem = resolveOwnedCartItem(user, cartItemId);
        return toCartItemResponse(cartItem);
    }

    /**
     * Updates the quantity of a cart item with stock validation and ownership verification.
     *
     * @param user       authenticated user
     * @param cartItemId cart item ID
     * @param request    update payload
     * @return updated cart item response
     * @throws CartItemNotFoundException     if the item does not exist
     * @throws CartItemAccessDeniedException if the item belongs to another user
     * @throws InsufficientStockException    if stock is insufficient
     */
    @Override
    public CartItemResponse update(User user, Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = resolveOwnedCartItem(user, cartItemId);
        validateStock(cartItem.getItemDetails(), request.quantity());
        cartItem.setQuantity(request.quantity());
        cartItemRepository.save(cartItem);
        return toCartItemResponse(cartItem);
    }

    /**
     * Deletes a cart item with ownership verification.
     *
     * @param user       authenticated user
     * @param cartItemId cart item ID
     * @throws CartItemNotFoundException     if the item does not exist
     * @throws CartItemAccessDeniedException if the item belongs to another user
     */
    @Override
    public void delete(User user, Long cartItemId) {
        CartItem cartItem = resolveOwnedCartItem(user, cartItemId);
        cartItemRepository.delete(cartItem);
    }

    /**
     * Removes all items from the user's cart via orphan removal.
     *
     * <p>If the user has no cart, the operation is silently skipped.</p>
     *
     * @param user the authenticated user whose cart should be cleared
     */
    @Override
    public void clearCart(User user) {
        cartRepository.findByUser(user).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    /**
     * Returns the user's cart, creating one if it does not yet exist.
     */
    private Cart findOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    try {
                        return cartRepository.save(
                                Cart.builder().user(user).build()
                        );
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        return cartRepository.findByUser(user)
                                .orElseThrow(() -> e);
                    }
                });
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
                .map(item -> PriceUtil.effectivePrice(item.getItemDetails())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Maps a {@link CartItem} to a {@link CartItemResponse}.
     *
     * <p>Accesses the related {@link Item}
     * via the inverse side of the ItemDetails → Item relationship.</p>
     */
    private CartItemResponse toCartItemResponse(CartItem cartItem) {
        ItemDetails details = cartItem.getItemDetails();
        BigDecimal unitPrice = PriceUtil.effectivePrice(details);
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
