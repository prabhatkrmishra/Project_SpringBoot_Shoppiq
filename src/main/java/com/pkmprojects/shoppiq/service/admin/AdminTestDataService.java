package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.admin.request.*;
import com.pkmprojects.shoppiq.dto.cart.CartItemResponse;
import com.pkmprojects.shoppiq.dto.item.ItemResponse;
import com.pkmprojects.shoppiq.dto.order.CheckoutResponse;
import com.pkmprojects.shoppiq.dto.review.ItemReviewResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerResponse;
import com.pkmprojects.shoppiq.dto.user.UserResponse;

import java.util.List;

/**
 * Business contract for generating and managing test data via bulk endpoints.
 *
 * <p>Defines transactional bulk-creation methods for users, items, addresses,
 * reviews, sellers, carts, and orders for development and testing.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface AdminTestDataService {

    /**
     * Creates multiple user accounts.
     *
     * @param request bulk user creation payload
     * @return list of created user responses
     */
    List<UserResponse> createBulkUsers(BulkUserRequest request);

    /**
     * Creates multiple items with auto-approval and seller assignment.
     *
     * @param request bulk item creation payload
     * @return list of created item responses
     */
    List<ItemResponse> createBulkItems(BulkAdminItemRequest request);

    /**
     * Creates multiple addresses for existing users.
     *
     * @param request bulk address creation payload
     * @return list of created address responses
     */
    List<AddressResponse> createBulkAddresses(BulkAddressRequest request);

    /**
     * Creates multiple product reviews.
     *
     * @param request bulk review creation payload
     * @return list of created review responses
     */
    List<ItemReviewResponse> createBulkReviews(BulkReviewRequest request);

    /**
     * Creates multiple seller profiles for existing users.
     *
     * @param request bulk seller creation payload
     * @return list of created seller responses
     */
    List<SellerResponse> createBulkSellers(BulkSellerRequest request);

    /**
     * Adds multiple items to user carts.
     *
     * @param request bulk cart item addition payload
     * @return list of created cart item responses
     */
    List<CartItemResponse> createBulkCartItems(BulkCartRequest request);

    /**
     * Creates multiple orders from user carts.
     *
     * @param request bulk order creation payload
     * @return list of checkout responses
     */
    List<CheckoutResponse> createBulkOrders(BulkOrderRequest request);
}
