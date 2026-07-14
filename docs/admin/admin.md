# Admin Dashboard Service

> **Framework:** Spring Boot 4.1 + Spring Data JPA
> **Security:** `@PreAuthorize("hasRole('ADMIN')")` on all endpoints
> **Pattern:** Centralized admin hub with specialized sub-controllers

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Key Components](#2-key-components)
3. [Thymeleaf Templates](#3-thymeleaf-templates)
4. [Dashboard Analytics](#4-dashboard-analytics)
5. [Inventory Management](#5-inventory-management)
6. [Order Management](#6-order-management)
7. [User Management](#7-user-management)
8. [Seller Management](#8-seller-management)
9. [Review Management](#9-review-management)
10. [Reporting System](#10-reporting-system)
11. [Promo Code Management](#11-promo-code-management)
12. [Banner Management](#12-banner-management)
13. [Contact Messages](#13-contact-messages)
14. [Admin Mail](#14-admin-mail)
15. [Test Data Bulk Import](#15-test-data-bulk-import)

---

## 1. Architecture Overview

### Controller Layer

| Controller | Path | Endpoints Count | Description |
|------------|------|-----------------|-------------|
| `AdminController` | `controller/admin/AdminController.java` | 30+ | Main dashboard, inventory, orders, users, payments, reviews, reports, test data |
| `AdminProductController` | `controller/admin/AdminProductController.java` | 3 | Pending product approval/rejection |
| `AdminSellerController` | `controller/admin/AdminSellerController.java` | 4 | Seller approval/suspension |
| `AdminBannerController` | `controller/admin/AdminBannerController.java` | 6 | Banner CRUD operations |
| `AdminContactMessageController` | `controller/admin/AdminContactMessageController.java` | 5 | Contact message management |
| `AdminMailController` | `controller/admin/AdminMailController.java` | 2 | Admin email functionality |
| `AdminPromoCodeController` | `controller/admin/AdminPromoCodeController.java` | 5 | Promo code management |
| `AdminAiChatController` | `controller/admin/AdminAiChatController.java` | 4 | AI chat conversation management |

### Service Layer

| Service | Path | Description |
|---------|------|-------------|
| `AdminDashboardServiceImpl` | `service/impl/AdminDashboardServiceImpl.java` | Dashboard metrics, sales analytics, recent activity |
| `AdminInventoryServiceImpl` | `service/impl/AdminInventoryServiceImpl.java` | Product inventory CRUD, stock adjustment |
| `AdminOrderServiceImpl` | `service/impl/AdminOrderServiceImpl.java` | Order listing, status updates |
| `AdminUserServiceImpl` | `service/impl/AdminUserServiceImpl.java` | Customer management, blocking/unblocking |
| `AdminPaymentServiceImpl` | `service/impl/AdminPaymentServiceImpl.java` | Payment management, refunds, statistics |
| `AdminReviewServiceImpl` | `service/impl/AdminReviewServiceImpl.java` | Review listing, approval, rejection, deletion |
| `AdminReportServiceImpl` | `service/impl/AdminReportServiceImpl.java` | Report generation and export |
| `AdminSellerServiceImpl` | `service/impl/AdminSellerServiceImpl.java` | Seller approval workflow |
| `AdminTestDataService` | `service/admin/AdminTestDataService.java` | Bulk test data creation |

---

## 2. Key Components

### AdminController Endpoints

| Method | Endpoint | Service Method | Description |
|--------|----------|---------------|-------------|
| `GET` | `/api/admin/dashboard/summary` | `dashboardService.getDashboardSummary()` | Returns `DashboardSummaryResponse` |
| `GET` | `/api/admin/dashboard/sales-analytics` | `dashboardService.getSalesAnalytics()` | Returns `SalesAnalyticsResponse` |
| `GET` | `/api/admin/dashboard/recent-activity` | `dashboardService.getRecentActivity()` | Returns `RecentActivityResponse` |

### AdminInventoryServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `getAllProductInventory(int page, int size)` | readOnly | Paginated product listing |
| `getLowStockProducts()` | readOnly | Products with stock ≤ 5 |
| `getOutOfStockProducts()` | readOnly | Products with stock = 0 |
| `adjustStock(Long itemId, StockAdjustmentRequest)` | read-write | Update stock quantity |
| `bulkUpdateStock(Map<Long, StockAdjustmentRequest>)` | read-write | Bulk stock updates |
| `toggleOnSale(Long itemId, boolean)` | read-write | Toggle sale flag |
| `updateDiscount(Long itemId, BigDecimal)` | read-write | Update discount percentage |
| `putOnSale(Long itemId, BigDecimal)` | read-write | Set on sale with discount |
| `bulkToggleOnSale(List<Long>, boolean, BigDecimal)` | read-write | Bulk sale operations |
| `getInventoryDashboardSummary()` | readOnly | Inventory statistics |

### Security Enforcement

All admin controllers use class-level `@PreAuthorize("hasRole('ADMIN')")"` ensuring only ADMIN role users can access. Individual endpoints validate ownership where applicable (e.g., blocking own account throws `AdminCannotBlockSelfException`).

---

## 3. Thymeleaf Templates

### Admin Pages

| Template | URL | Controller Mapping | Purpose |
|----------|-----|------------------|---------|
| `admin-dashboard.html` | `/admin/dashboard` | `FrontEndController.adminDashboardPage()` | Main admin dashboard |
| `admin-inventory.html` | `/admin/inventory` | `FrontEndController.adminInventoryPage()` | Inventory management UI |
| `admin-orders.html` | `/admin/orders` | `FrontEndController.adminOrdersPage()` | Order listing & management |
| `admin-users.html` | `/admin/users` | `FrontEndController.adminUsersPage()` | Customer management |
| `admin-payments.html` | `/admin/payments` | `FrontEndController.adminPaymentsPage()` | Payment management |
| `admin-reviews.html` | `/admin/reviews` | `FrontEndController.adminReviewsPage()` | Review moderation |
| `admin-reports.html` | `/admin/reports` | `FrontEndController.adminReportsPage()` | Report generation UI |
| `admin-categories.html` | `/admin/categories` | `FrontEndController.adminCategoriesPage()` | Category management |
| `admin-category-import.html` | `/admin/categories/import` | `FrontEndController.adminCategoryImportPage()` | Bulk category import |
| `admin-pending.html` | `/admin/pending` | `FrontEndController.adminPendingPage()` | Pending approvals |
| `admin-products.html` | `/admin/products` | `FrontEndController.adminProductsPage()` | Product approval queue |
| `admin-sale.html` | `/admin/sale` | `FrontEndController.adminSalePage()` | Sale management |
| `admin-roles.html` | `/admin/roles` | `FrontEndController.adminRolesPage()` | Role management |
| `admin-promo-codes.html` | `/admin/promo-codes` | `FrontEndController.adminPromoCodesPage()` | Promo code CRUD |
| `admin-banners.html` | `/admin/banners` | `FrontEndController.adminBannersPage()` | Banner management |
| `admin-mail.html` | `/admin/mail` | `FrontEndController.adminMailPage()` | Admin email composer |
| `admin-messages.html` | `/admin/messages` | `FrontEndController.adminMessagesPage()` | Contact messages |
| `admin-ai-chats.html` | `/admin/ai-chats` | `FrontEndController.adminAiChatsPage()` | AI chat listing |
| `admin-ai-chat-detail.html` | `/admin/ai-chats/{chatId}` | `FrontEndController.adminAiChatDetailPage()` | Chat conversation detail |

---

## 4. Dashboard Analytics

### Endpoints

| Method | Endpoint | Controller Method | Service Method | Response DTO |
|--------|----------|-------------------|--------------|--------------|
| `GET` | `/api/admin/dashboard/summary` | `getDashboardSummary()` | `dashboardService.getDashboardSummary()` | `DashboardSummaryResponse` |
| `GET` | `/api/admin/dashboard/sales-analytics` | `getSalesAnalytics()` | `dashboardService.getSalesAnalytics()` | `SalesAnalyticsResponse` |
| `GET` | `/api/admin/dashboard/recent-activity` | `getRecentActivity()` | `dashboardService.getRecentActivity()` | `RecentActivityResponse` |

### DashboardSummaryResponse Fields

| Field | Type | Source |
|-------|------|--------|
| `totalUsers` | Long | `userRepository.count()` |
| `todaysOrders` | Long | Orders placed today |
| `totalOrders` | Long | `orderRepository.count()` |
| `totalProducts` | Long | `itemRepository.count()` |
| `todaysRevenue` | BigDecimal | Sum of PAID payments today |
| `totalRevenue` | BigDecimal | Sum of all PAID payments |
| `pendingOrders` | Long | Orders with PLACED status |
| `cancelledOrders` | Long | Orders with CANCELLED status |
| `outOfStockProducts` | Long | Items with stock = 0 |
| `lowStockProducts` | Long | Items with 0 < stock ≤ 5 |

### SalesAnalyticsResponse Fields

| Field | Type | Description |
|-------|------|-------------|
| `dailySales` | List\<DailySalesData\> | Last 30 days of daily sales |
| `weeklySales` | List\<WeeklySalesData\> | Weekly aggregated sales |
| `monthlySales` | List\<MonthlySalesData\> | Monthly aggregated sales |
| `topSellingProducts` | List\<TopSellingProductData\> | Top 10 by quantity sold |
| `topCategories` | List\<TopCategoryData\> | Top 10 categories by revenue |
| `revenueTrends` | Map\<LocalDate, BigDecimal\> | Daily revenue for last 30 days |
| `todayRevenue` | BigDecimal | Revenue today |
| `weekRevenue` | BigDecimal | Revenue last 7 days |
| `monthRevenue` | BigDecimal | Revenue last 30 days |

---

## 5. Inventory Management

### Endpoints

| Method | Endpoint | Controller Method | Service Method | Description |
|--------|----------|-------------------|---------------|-------------|
| `GET` | `/api/admin/inventory` | `getAllInventory()` | `inventoryService.getAllProductInventory()` | Paginated product listing |
| `GET` | `/api/admin/inventory/low-stock` | `getLowStockProducts()` | `inventoryService.getLowStockProducts()` | Products with stock ≤ 5 |
| `GET` | `/api/admin/inventory/out-of-stock` | `getOutOfStockProducts()` | `inventoryService.getOutOfStockProducts()` | Products with stock = 0 |
| `PUT` | `/api/admin/inventory/{itemId}` | `adjustStock()` | `inventoryService.adjustStock()` | Update stock quantity |
| `POST` | `/api/admin/inventory/bulk-adjust` | `bulkAdjustStock()` | `inventoryService.bulkUpdateStock()` | Bulk stock adjustment |
| `PUT` | `/api/admin/inventory/{itemId}/on-sale` | `toggleOnSale()` | `inventoryService.toggleOnSale()` | Toggle sale flag |
| `PUT` | `/api/admin/inventory/{itemId}/discount` | `updateDiscount()` | `inventoryService.updateDiscount()` | Update discount % |
| `PUT` | `/api/admin/inventory/{itemId}/put-on-sale` | `putOnSale()` | `inventoryService.putOnSale()` | Set on sale with discount |
| `PUT` | `/api/admin/inventory/bulk-on-sale` | `bulkToggleOnSale()` | `inventoryService.bulkToggleOnSale()` | Bulk sale operations |
| `GET` | `/api/admin/inventory/summary` | `getInventorySummary()` | `inventoryService.getInventoryDashboardSummary()` | Inventory statistics |

### Request DTOs

**StockAdjustmentRequest** (`dto/admin/request/StockAdjustmentRequest.java`):
- `quantity`: int - New stock quantity
- `reason`: String - Reason for adjustment (optional)

### Response DTO

**AdminProductInventoryResponse** (`dto/admin/response/AdminProductInventoryResponse.java`):
- `id`, `name`, `slug`, `description` - Product info
- `categoryName`, `sku`, `brand` - ItemDetails fields
- `price`, `discountPercentage` - Pricing
- `stockQuantity`, `lowStockThreshold` - Stock info
- `imageUrl`, `onSale` - Display fields

---

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/inventory` | All products (paginated) |
| `GET` | `/api/admin/inventory/low-stock` | Low stock products |
| `GET` | `/api/admin/inventory/out-of-stock` | Out of stock products |
| `PUT` | `/api/admin/inventory/{itemId}` | Adjust stock |
| `POST` | `/api/admin/inventory/bulk-adjust` | Bulk stock adjustment |
| `PUT` | `/api/admin/inventory/{itemId}/on-sale` | Toggle on-sale flag |
| `PUT` | `/api/admin/inventory/{itemId}/discount` | Update discount % |
| `PUT` | `/api/admin/inventory/{itemId}/put-on-sale` | Put on sale with discount |
| `PUT` | `/api/admin/inventory/bulk-on-sale` | Bulk toggle on-sale |
| `GET` | `/api/admin/inventory/summary` | Inventory summary |

### Bulk Stock Adjustment

```json
// Request
{
  "adjustments": [
    { "itemId": 1, "quantity": 50, "reason": "Restocked" },
    { "itemId": 2, "quantity": -10, "reason": "Damaged" }
  ]
}
```

---

## 6. Order Management

### Endpoints

| Method | Endpoint | Controller Method | Service Method | Response DTO |
|--------|----------|-------------------|--------------|--------------|
| `GET` | `/api/admin/orders` | `getAllOrders()` | `orderService.getAllOrders()` | `PageResponse<AdminOrderResponse>` |
| `GET` | `/api/admin/orders/{orderId}` | `getOrderById()` | `orderService.getOrderById()` | `AdminOrderResponse` |
| `PUT` | `/api/admin/orders/{orderId}/status` | `updateOrderStatus()` | `orderService.updateOrderStatus()` | `AdminOrderResponse` |

### AdminOrderServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `getAllOrders(OrderStatus, int, int)` | readOnly | Paginated order listing filtered by status |
| `getOrderById(Long)` | readOnly | Single order with items and addresses |
| `updateOrderStatus(Long, OrderStatus)` | read-write | Valid status transitions enforced |

### Order Status Transitions

| Current Status | Allowed Next Status |
|----------------|---------------------|
| PLACED | PROCESSING, SHIPPED, DELIVERED, CANCELLED |
| PROCESSING | SHIPPED, DELIVERED, CANCELLED |
| SHIPPED | DELIVERED, CANCELLED |
| Any | CANCELLED |

### AdminOrderResponse Fields

| Field | Type | Source |
|-------|------|--------|
| `id`, `grandTotal`, `status`, `placedAt` | - | Order entity |
| `customerUsername`, `customerEmail` | String | User from order |
| `items` | List\<AdminOrderItem\> | Order items with snapshots |
| `shippingAddress` | AdminAddressItem | OrderAddressSnapshot |

---

## 7. User Management

### Endpoints

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `GET` | `/api/admin/users` | `getAllCustomers()` | `userService.getAllCustomers()` |
| `GET` | `/api/admin/users/{userId}` | `getCustomerById()` | `userService.getCustomerById()` |
| `PUT` | `/api/admin/users/{userId}/block` | `blockCustomer()` | `userService.blockCustomer()` |
| `PUT` | `/api/admin/users/{userId}/unblock` | `unblockCustomer()` | `userService.unblockCustomer()` |
| `GET` | `/api/admin/users/stats` | `getCustomerStats()` | `userService.getCustomerDashboardStats()` |

### AdminUserServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `getAllCustomers(Boolean, int, int)` | readOnly | Paginated user listing, filter by enabled status |
| `getCustomerById(Long)` | readOnly | User profile details |
| `blockCustomer(Long)` | read-write | Sets `enabled=false` |
| `unblockCustomer(Long)` | read-write | Sets `enabled=true` |
| `getCustomerDashboardStats()` | readOnly | Returns `CustomerDashboardStats` record |

### CustomerDashboardStats

| Field | Type | Description |
|-------|------|-------------|
| `totalCustomers` | Long | Total user count |
| `activeCustomers` | Long | Non-blocked users |
| `blockedCustomers` | Long | Blocked users |
| `newCustomersToday` | Long | Users created today |
| `newCustomersThisWeek` | Long | Users created this week |
| `newCustomersThisMonth` | Long | Users created this month |

### Self-Protection

Admin cannot block/unblock themselves:
```java
// AdminController.java:319-327
if (userId.equals(currentUser.getId())) {
    throw AdminCannotBlockSelfException.block(sellerId);
}
```

---

## 8. Seller Management

### Endpoints

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `GET` | `/api/admin/sellers` | `getSellers()` | `adminSellerService.getAllSellers()` / `getSellersByStatus()` |
| `PUT` | `/api/admin/sellers/{sellerId}/approve` | `approveSeller()` | `adminSellerService.approveSeller()` |
| `PUT` | `/api/admin/sellers/{sellerId}/reject` | `rejectSeller()` | `adminSellerService.rejectSeller()` |
| `PUT` | `/api/admin/sellers/{sellerId}/suspend` | `suspendSeller()` | `adminSellerService.suspendSeller()` |
| `PUT` | `/api/admin/sellers/{sellerId}/unsuspend` | `unsuspendSeller()` | `adminSellerService.unsuspendSeller()` |

### AdminSellerServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `getAllSellers(int, int)` | readOnly | Paginated seller listing |
| `getSellersByStatus(VerificationStatus, int, int)` | readOnly | Filter by PENDING/APPROVED/REJECTED |
| `approveSeller(Long)` | read-write | Sets VerificationStatus=APPROVED, SellerStatus=ACTIVE, creates Store |
| `rejectSeller(Long)` | read-write | Sets VerificationStatus=REJECTED, SellerStatus=INACTIVE |
| `suspendSeller(Long)` | read-write | Sets SellerStatus=SUSPENDED, Store status=SUSPENDED |
| `unsuspendSeller(Long)` | read-write | Sets SellerStatus=ACTIVE, Store status=DRAFT |

### Two-Dimensional Status Model

| Status Type | Values | Meaning |
|-------------|--------|---------|
| VerificationStatus | PENDING, APPROVED, REJECTED | Can they sell? |
| SellerStatus | ACTIVE, SUSPENDED, INACTIVE | Are they active? |

A seller is only **ACTIVE** after being **APPROVED**.

### AdminSellerResponse Fields

| Field | Type | Source |
|-------|------|--------|
| `id`, `businessName`, `businessEmail` | String | Seller entity |
| `phone`, `gstNumber`, `panNumber` | - | Business info |
| `commissionRate`, `rating` | BigDecimal | Seller config |
| `verificationStatus`, `sellerStatus` | Enum | Status fields |
| `joinedAt` | Instant | Registration timestamp |

---

## 9. Review Management

### Endpoints

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `GET` | `/api/admin/reviews` | `getAllReviews()` | `reviewService.getAllReviews()` |
| `DELETE` | `/api/admin/reviews/{reviewId}` | `deleteReview()` | `reviewService.deleteReview()` |
| `PUT` | `/api/admin/reviews/{reviewId}/approve` | `approveReview()` | `reviewService.approveReview()` |
| `PUT` | `/api/admin/reviews/{reviewId}/reject` | `rejectReview()` | `reviewService.rejectReview()` |

### AdminReviewServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `getAllReviews(int, int)` | readOnly | Paginated review listing |
| `deleteReview(Long)` | read-write | Hard delete review record |
| `approveReview(Long)` | read-write | Sets ReviewStatus=APPROVED |
| `rejectReview(Long)` | read-write | Sets ReviewStatus=REJECTED |

### Review Status Flow

```
PENDING → (Admin approves) → APPROVED
    ↓
PENDING → (Admin rejects) → REJECTED
```

### AdminReviewResponse Fields

| Field | Type | Source |
|-------|------|--------|
| `id`, `rating` | - | ItemReview entity |
| `itemName` | String | Item from review |
| `itemSlug` | String | Item slug |
| `customerUsername` | String | User from review |
| `status`, `createdAt` | Enum/Instant | Review fields |

---

## 7. Review Management

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/reviews` | All reviews (paginated) |
| `DELETE` | `/api/admin/reviews/{reviewId}` | Delete review |
| `PUT` | `/api/admin/reviews/{reviewId}/approve` | Approve review |
| `PUT` | `/api/admin/reviews/{reviewId}/reject` | Reject review |

### Review Status Flow

```
PENDING → (Admin approves) → APPROVED
    ↓
PENDING → (Admin rejects) → REJECTED
```

---

## 10. Reporting System

### Endpoints

| Method | Endpoint | Controller Method | Service Method | Response Type |
|--------|----------|-------------------|--------------|--------------|
| `GET` | `/api/admin/reports/sales` | `getSalesReport()` | `reportService.generateSalesReport()` | `SalesReport` |
| `GET` | `/api/admin/reports/revenue` | `getRevenueReport()` | `reportService.generateRevenueReport()` | `RevenueReport` |
| `GET` | `/api/admin/reports/products` | `getProductReport()` | `reportService.generateProductReport()` | `ProductReport` |
| `GET` | `/api/admin/reports/customers` | `getCustomerReport()` | `reportService.generateCustomerReport()` | `CustomerReport` |
| `GET` | `/api/admin/reports/inventory` | `getInventoryReport()` | `reportService.generateInventoryReport()` | `InventoryReport` |
| `GET` | `/api/admin/reports/commission` | `getCommissionReport()` | `reportService.generateCommissionReport()` | `List<CommissionReportResponse>` |
| `GET` | `/api/admin/reports/export` | `exportReport()` | `reportService.exportReport()` | `byte[]` |

### AdminReportServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `generateSalesReport(LocalDate, LocalDate)` | readOnly | Daily/weekly/monthly sales, top products/categories |
| `generateRevenueReport(LocalDate, LocalDate)` | readOnly | Revenue by payment status/method, discounts/taxes |
| `generateProductReport(LocalDate, LocalDate)` | readOnly | Product performance, category breakdown |
| `generateCustomerReport(LocalDate, LocalDate)` | readOnly | Top customers, segments (VIP/Regular/New) |
| `generateInventoryReport()` | readOnly | Inventory value, stock status breakdown |
| `generateCommissionReport()` | readOnly | Seller revenue/commission calculations |
| `exportReport(ReportType, ExportFormat, LocalDate, LocalDate)` | readOnly | Returns PDF/Excel/CSV bytes |

### SalesReport Fields

| Field | Type | Description |
|-------|------|-------------|
| `startDate`, `endDate` | LocalDate | Report period |
| `totalOrders`, `totalRevenue` | Long/BigDecimal | Totals |
| `dailySales` | Map\<LocalDate, DailySales\> | Daily breakdown |
| `ordersByStatus` | Map\<OrderStatus, Long\> | Status counts |
| `topProducts`, `topCategories` | List | Ranked by quantity/revenue |

### RevenueReport Fields

| Field | Type | Description |
|-------|------|-------------|
| `dailyRevenue` | Map\<LocalDate, BigDecimal\> | Daily totals |
| `revenueByPaymentStatus` | Map\<PaymentStatus, BigDecimal\> | Status breakdown |
| `revenueByPaymentMethod` | Map\<String, BigDecimal\> | Method breakdown |

---

## 11. Promo Code Management

### Endpoints

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `POST` | `/api/admin/promo-codes` | `create()` | `promoCodeService.create()` |
| `GET` | `/api/admin/promo-codes` | `findAll()` | `promoCodeService.findAll()` |
| `GET` | `/api/admin/promo-codes/{id}` | `findById()` | `promoCodeService.findById()` |
| `PATCH` | `/api/admin/promo-codes/{id}/toggle` | `toggleActive()` | `promoCodeService.toggleActive()` |
| `DELETE` | `/api/admin/promo-codes/{id}` | `delete()` | `promoCodeService.delete()` |

### PromoCodeServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `create(PromoCodeRequest)` | read-write | Validates uniqueness, creates PromoCode entity |
| `findAll(int, int)` | readOnly | Paginated listing |
| `findById(Long)` | readOnly | Single promo code |
| `toggleActive(Long)` | read-write | Flips `active` boolean |
| `delete(Long)` | read-write | Deletes promo code |

### PromoCode Properties

| Property | Type | Constraints | Notes |
|----------|------|-------------|-------|
| `code` | String | Unique | Promo code identifier |
| `discountType` | DiscountType | - | PERCENTAGE or FIXED |
| `discountValue` | BigDecimal | - | Discount amount/% |
| `minOrderAmount` | BigDecimal | - | Minimum order required |
| `maxDiscountAmount` | BigDecimal | - | Maximum discount cap |
| `usageLimit` | int | - | Total usage limit |
| `userUsageLimit` | int | - | Per-user usage limit |
| `validFrom`, `validUntil` | Instant | - | Validity period |
| `active` | boolean | - | Enabled/disabled |

---

## 12. Banner Management

### Endpoints

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `GET` | `/api/admin/banners` | `findAll()` | `bannerService.findAll()` |
| `GET` | `/api/admin/banners/{id}` | `findById()` | `bannerService.findById()` |
| `POST` | `/api/admin/banners` | `create()` | `bannerService.create()` |
| `PUT` | `/api/admin/banners/{id}` | `update()` | `bannerService.update()` |
| `PATCH` | `/api/admin/banners/{id}/toggle` | `toggleActive()` | `bannerService.toggleActive()` |
| `DELETE` | `/api/admin/banners/{id}` | `delete()` | `bannerService.delete()` |

### BannerServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `findAll(int, int)` | readOnly | Paginated banner listing |
| `findById(Long)` | readOnly | Single banner |
| `create(BannerRequest)` | read-write | Creates banner with title, image, link |
| `update(Long, BannerRequest)` | read-write | Updates mutable fields |
| `toggleActive(Long)` | read-write | Flips `active` flag |
| `delete(Long)` | read-write | Deletes banner |

### Banner Properties

| Property | Type | Description |
|----------|------|-------------|
| `title` | String | Banner heading |
| `imageUrl` | String | Image location |
| `linkUrl` | String | Click-through URL |
| `bannerType` | BannerType | Type classification |
| `active` | boolean | Visibility flag |

---

## 13. Contact Messages

### Endpoints

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `GET` | `/api/admin/messages/unread-count` | `getUnreadCount()` | `contactMessageService.countUnreadMessages()` |
| `GET` | `/api/admin/messages` | `getAllMessages()` | `contactMessageService.getAllMessages()` |
| `GET` | `/api/admin/messages/{id}` | `getMessageById()` | `contactMessageService.getMessageById()` |
| `DELETE` | `/api/admin/messages/{id}` | `deleteMessage()` | `contactMessageService.deleteMessage()` |
| `PUT` | `/api/admin/messages/{id}/read` | `markAsRead()` | `contactMessageService.markAsRead()` |
| `PUT` | `/api/admin/messages/{id}/unread` | `markAsUnread()` | `contactMessageService.markAsUnread()` |

### ContactMessageServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `countUnreadMessages()` | readOnly | Count of messages where `status=UNREAD` |
| `getAllMessages(int, int)` | readOnly | Paginated message listing |
| `getMessageById(Long)` | readOnly | Single message |
| `deleteMessage(Long)` | read-write | Deletes message |
| `markAsRead(Long)` | read-write | Sets status to READ |
| `markAsUnread(Long)` | read-write | Sets status to UNREAD |

### ContactMessageStatus

| Status | Description |
|--------|-------------|
| `UNREAD` | New message, not yet viewed |
| `READ` | Message has been viewed |

---

## 14. Admin Mail

### Endpoints

| Method | Endpoint | Controller Method | Service |
|--------|----------|-------------------|---------|
| `GET` | `/api/admin/mail/search?q=` | `searchUsers()` | `UserRepository` |
| `POST` | `/api/admin/mail/send` | `sendMail()` | `AdminMailService` |

### AdminMailService Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `sendMail(AdminMailRequest, String)` | async | Background email sending for large lists |

### AdminMailRequest Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `userIds` | List\<Long\> | Conditional | Recipients (empty + sendToAll=true for all) |
| `sendToAll` | Boolean | No | Send to all users |
| `subject` | String | Yes | Email subject |
| `message` | String | Yes | Email body (HTML) |

---

## 15. Test Data Bulk Import

### Endpoints

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|----------------|
| `POST` | `/api/admin/test/items/bulk` | `createBulkItems()` | `testDataService.createBulkItems()` |
| `POST` | `/api/admin/test/categories/bulk` | `createBulkCategories()` | `categoryService.createBulk()` |
| `POST` | `/api/admin/test/users/bulk` | `createBulkUsers()` | `testDataService.createBulkUsers()` |
| `POST` | `/api/admin/test/addresses/bulk` | `createBulkAddresses()` | `testDataService.createBulkAddresses()` |
| `POST` | `/api/admin/test/reviews/bulk` | `createBulkReviews()` | `testDataService.createBulkReviews()` |
| `POST` | `/api/admin/test/sellers/bulk` | `createBulkSellers()` | `testDataService.createBulkSellers()` |
| `POST` | `/api/admin/test/carts/bulk` | `createBulkCartItems()` | `testDataService.createBulkCartItems()` |
| `POST` | `/api/admin/test/orders/bulk` | `createBulkOrders()` | `testDataService.createBulkOrders()` |

### AdminTestDataServiceImpl Methods

| Method | Description |
|--------|-------------|
| `createBulkItems(BulkAdminItemRequest)` | Creates Item + ItemDetails with categories |
| `createBulkCategories(BulkCategoryRequest)` | Batch creates categories |
| `createBulkUsers(BulkUserRequest)` | Creates users with roles |
| `createBulkAddresses(BulkAddressRequest)` | Associates addresses with users |
| `createBulkReviews(BulkReviewRequest)` | Creates reviews for items |
| `createBulkSellers(BulkSellerRequest)` | Creates sellers with details |
| `createBulkCartItems(BulkCartRequest)` | Populates cart items |
| `createBulkOrders(BulkOrderRequest)` | Creates orders with items |

### Request DTOs

All bulk request DTOs use records with list fields:
- `BulkAdminItemRequest(items: List<BulkItemRequest>)`
- `BulkCategoryRequest(categories: List<CategoryRequest>)`
- `BulkUserRequest(users: List<UserRequest>)`
- etc.
