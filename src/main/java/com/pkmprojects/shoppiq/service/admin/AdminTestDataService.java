package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.order.CheckoutResponse;
import com.pkmprojects.shoppiq.dto.response.ItemResponse;
import com.pkmprojects.shoppiq.dto.request.*;
import com.pkmprojects.shoppiq.dto.response.CartItemResponse;
import com.pkmprojects.shoppiq.dto.response.ItemReviewResponse;
import com.pkmprojects.shoppiq.dto.response.UserResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerResponse;

import java.util.List;

/**
 * Admin service for populating test data via bulk endpoints.
 *
 * <p>
 * Provides transactional bulk-creation methods for all major entities.
 * Each method accepts a dedicated bulk request DTO and returns a list
 * of response DTOs for the created entities.
 * </p>
 *
 * <h2>Endpoint Design</h2>
 * <ul>
 *     <li>All endpoints are mapped under {@code /api/admin/test/}.</li>
 *     <li>User context is supplied inline (no AuthenticationPrincipal).</li>
 *     <li>Each item in the request specifies the target user by ID.</li>
 * </ul>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Create users in bulk with encoded passwords and default roles.</li>
 *     <li>Create addresses in bulk for existing users.</li>
 *     <li>Create product reviews in bulk for existing users and items.</li>
 *     <li>Create seller profiles in bulk for existing users.</li>
 *     <li>Add items to user carts in bulk.</li>
 *     <li>Create orders from user carts in bulk.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
