package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminUserResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;

import java.math.BigDecimal;

/**
 * Business contract for admin customer management.
 *
 * <p>
 * Defines the operations for managing customer accounts,
 * including retrieval, blocking, and unblocking.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Retrieve all customers with pagination.</li>
 *     <li>Retrieve a single customer by ID.</li>
 *     <li>Block a customer account.</li>
 *     <li>Unblock a customer account.</li>
 *     <li>Get customer dashboard statistics.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Works exclusively with DTOs.</li>
 *     <li>Blocking disables login but preserves data.</li>
 *     <li>Implemented by {@code AdminUserServiceImpl}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface AdminUserService {

    /**
     * Retrieves all customers with optional filtering.
     *
     * @param enabled optional enabled filter
     * @param page    page number (0-based)
     * @param size    page size
     * @return paginated customer responses
     */
    PageResponse<AdminUserResponse> getAllCustomers(Boolean enabled, int page, int size);

    /**
     * Retrieves a single customer by ID.
     *
     * @param userId user identifier
     * @return customer response
     */
    AdminUserResponse getCustomerById(Long userId);

    /**
     * Blocks a customer account (disables login).
     *
     * @param userId user identifier
     * @return updated customer response
     */
    AdminUserResponse blockCustomer(Long userId);

    /**
     * Unblocks a customer account (enables login).
     *
     * @param userId user identifier
     * @return updated customer response
     */
    AdminUserResponse unblockCustomer(Long userId);

    /**
     * Retrieves customer dashboard statistics.
     *
     * @return customer statistics
     */
    CustomerDashboardStats getCustomerDashboardStats();

    /**
     * Customer dashboard statistics.
     */
    record CustomerDashboardStats(
            long totalCustomers,
            long activeCustomers,
            long blockedCustomers,
            long newCustomersThisMonth,
            BigDecimal totalRevenue,
            BigDecimal averageOrderValue
    ) {
    }
}