# Seller Service

> **Framework:** Spring Boot 4.1 + Spring Data JPA
> **Entities:** Seller, Store
> **Pattern:** Two-dimensional status (VerificationStatus + SellerStatus)

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Key Components](#2-key-components)
3. [Thymeleaf Templates](#3-thymeleaf-templates)
4. [Seller Entity](#4-seller-entity)
5. [Seller Approval Workflow](#5-seller-approval-workflow)
6. [REST API Endpoints](#6-rest-api-endpoints)
7. [Dashboard Features](#7-dashboard-features)
8. [Inventory Management](#8-inventory-management)
9. [Order Management](#9-order-management)

---

## 1. Architecture Overview

### Controller Layer

| Controller | Path | Endpoints | Description |
|------------|------|-----------|-------------|
| `SellerController` | `controller/seller/SellerController.java` | 4 | Seller registration, profile management |
| `SellerProductController` | `controller/seller/SellerProductController.java` | 4 | Product CRUD for sellers |
| `SellerInventoryController` | `controller/seller/SellerInventoryController.java` | 4 | Stock management |
| `SellerOrderController` | `controller/seller/SellerOrderController.java` | 3 | Order listing and status |
| `SellerDashboardController` | `controller/seller/SellerDashboardController.java` | 2 | Dashboard metrics |

### Service Layer

| Service | Path | Description |
|---------|------|-------------|
| `SellerServiceImpl` | `service/impl/SellerServiceImpl.java` | Registration, profile updates |
| `SellerProductServiceImpl` | `service/impl/SellerProductServiceImpl.java` | Product CRUD |
| `SellerInventoryServiceImpl` | `service/impl/SellerInventoryServiceImpl.java` | Stock queries and adjustments |
| `SellerOrderServiceImpl` | `service/impl/SellerOrderServiceImpl.java` | Order queries and status |
| `SellerDashboardServiceImpl` | `service/impl/SellerDashboardServiceImpl.java` | Dashboard metrics |

---

## 2. Key Components

### SellerController Endpoints

| Method | Endpoint | Service Method | Auth |
|--------|----------|---------------|------|
| `POST` | `/seller/register` | `sellerService.register()` | Any authenticated user |
| `GET` | `/seller/profile` | `sellerService.getProfile()` | SELLER/ADMIN |
| `PUT` | `/seller/update` | `sellerService.updateProfile()` | SELLER/ADMIN |
| `DELETE` | `/seller/delete` | `sellerService.deleteProfile()` | SELLER/ADMIN |
| `PUT` | `/seller/store/publish` | `sellerService.publishStore()` | SELLER/ADMIN |

### SellerServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `register(SellerRegistrationRequest, User)` | read-write | Creates Seller with PENDING status |
| `getProfile(User)` | readOnly | Returns SellerResponse |
| `updateProfile(SellerProfileUpdateRequest, User)` | read-write | Updates business info |
| `deleteProfile(User)` | read-write | Sets SellerStatus=INACTIVE |
| `publishStore(User)` | read-write | Creates/updates Store entity |

### SellerProductController Endpoints

| Method | Endpoint | Service Method |
|--------|----------|---------------|
| `POST` | `/seller/products/create` | `sellerProductService.createProduct()` |
| `GET` | `/seller/products` | `sellerProductService.getMyProducts()` |
| `GET` | `/seller/products/{id}` | `sellerProductService.getMyProductById()` |
| `PUT` | `/seller/products/update/{id}` | `sellerProductService.updateProduct()` |
| `DELETE` | `/seller/products/delete/{id}` | `sellerProductService.deleteProduct()` |

### SellerInventoryController Endpoints

| Method | Endpoint | Service Method |
|--------|----------|---------------|
| `GET` | `/seller/inventory` | `sellerInventoryService.getInventory()` |
| `GET` | `/seller/inventory/low-stock` | `sellerInventoryService.getLowStockProducts()` |
| `GET` | `/seller/inventory/out-of-stock` | `sellerInventoryService.getOutOfStockProducts()` |
| `PUT` | `/seller/inventory/{id}/adjust` | `sellerInventoryService.adjustStock()` |

---

## 3. Thymeleaf Templates

| Template | URL | Controller Mapping | Purpose |
|----------|-----|-------------------|---------|
| `seller/dashboard.html` | `/seller-panel/dashboard` | `FrontEndController.sellerDashboardPage()` | Seller dashboard |
| `seller/products.html` | `/seller-panel/products` | `FrontEndController.sellerProductsPage()` | Product management |
| `seller/inventory.html` | `/seller-panel/inventory` | `FrontEndController.sellerInventoryPage()` | Stock management |
| `seller/orders.html` | `/seller-panel/orders` | `FrontEndController.sellerOrdersPage()` | Order listing |
| `seller/order-detail.html` | `/seller-panel/order-detail` | `FrontEndController.sellerOrderDetailPage()` | Order details |
| `seller/profile.html` | `/seller-panel/profile` | `FrontEndController.sellerProfilePage()` | Profile management |

---

---

## 4. Seller Entity

### Seller Entity

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `user` | User | `NOT NULL`, unique FK | One-to-one with User |
| `businessName` | String | `NOT NULL` | Business display name |
| `businessEmail` | String | `NOT NULL` | Business contact email |
| `phone` | String | max 20 | Business phone number |
| `gstNumber` | String | optional | GST registration number |
| `panNumber` | String | `NOT NULL`, 10 chars | PAN card number |
| `commissionRate` | BigDecimal | precision 5, scale 2 | Platform commission % |
| `rating` | BigDecimal | precision 3, scale 2 | Average rating |
| `joinedAt` | Instant | `NOT NULL` | Registration timestamp |
| `businessAddress` | Address | lazy, optional | Business location |
| `verificationStatus` | VerificationStatus | `NOT NULL` | PENDING/APPROVED/REJECTED |
| `sellerStatus` | SellerStatus | `NOT NULL` | ACTIVE/SUSPENDED/INACTIVE |

### Two-Dimensional Status Model

| Status Type | Values | Meaning |
|-------------|--------|---------|
| VerificationStatus | PENDING, APPROVED, REJECTED | Can they sell? |
| SellerStatus | ACTIVE, SUSPENDED, INACTIVE | Are they active? |

A seller is only **ACTIVE** after being **APPROVED**.

---

## 5. Seller Approval Workflow

### Registration Flow

| Step | Endpoint | Service | Action |
|------|----------|---------|--------|
| 1 | `POST /seller/register` | `SellerServiceImpl.register()` | Creates Seller PENDING/INACTIVE |
| 2 | `GET /api/admin/sellers?status=PENDING` | `AdminSellerServiceImpl.getSellersByStatus()` | Admin views pending |
| 3 | `PUT /api/admin/sellers/{id}/approve` | `AdminSellerServiceImpl.approveSeller()` | APPROVED + ACTIVE + Store created |
| 4 | `PUT /api/admin/sellers/{id}/reject` | `AdminSellerServiceImpl.rejectSeller()` | REJECTED + INACTIVE |

### Product Approval Flow

| Step | Endpoint | Service | Action |
|------|----------|---------|--------|
| 1 | `POST /seller/products/create` | `SellerProductServiceImpl.createProduct()` | Product DRAFT status |
| 2 | `GET /api/admin/products/pending` | `AdminProductServiceImpl.getPendingProducts()` | Admin views pending |
| 3 | `PUT /api/admin/products/{id}/publish` | `AdminProductServiceImpl.publishProduct()` | PUBLISHED status |
| 4 | `PUT /api/admin/products/{id}/reject` | `AdminProductServiceImpl.rejectProduct()` | Rejected |

---

## 6. REST API Endpoints

### Seller Profile (`/seller`)

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `POST` | `/seller/register` | `register()` | `sellerService.register()` |
| `GET` | `/seller/profile` | `getProfile()` | `sellerService.getProfile()` |
| `PUT` | `/seller/update` | `updateProfile()` | `sellerService.updateProfile()` |
| `DELETE` | `/seller/delete` | `deleteProfile()` | `sellerService.deleteProfile()` |
| `PUT` | `/seller/store/publish` | `publishStore()` | `sellerService.publishStore()` |

### Seller Dashboard (`/seller/dashboard`)

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `GET` | `/seller/dashboard/summary` | `getDashboardSummary()` | `dashboardService.getDashboardSummary()` |
| `GET` | `/seller/dashboard/recent-orders` | `getRecentOrders()` | `dashboardService.getRecentOrders()` |

---

## 7. Dashboard Features

### SellerDashboardResponse Fields

| Field | Type | Source |
|-------|------|--------|
| `totalProducts` | Long | `itemRepository.countBySellerId()` |
| `totalOrders` | Long | `orderRepository.countDistinctBySellerId()` |
| `totalRevenue` | BigDecimal | `orderItemRepository.sumRevenueBySellerIdAndPaymentStatus(PAID)` |
| `lowStockProducts` | Long | `itemDetailsRepository.findLowStockProductsBySellerId()` |
| `outOfStockProducts` | Long | `itemDetailsRepository.findOutOfStockProductsBySellerId()` |

### SellerDashboardServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `getDashboardSummary(User)` | readOnly | Aggregates seller metrics |
| `getRecentOrders(User)` | readOnly | Last 10 orders by seller |

---

## 8. Inventory Management

### SellerInventoryServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `getInventory(User, int, int)` | readOnly | Paginated seller products |
| `getLowStockProducts(User, int, int)` | readOnly | Products with 0 < stock ≤ 5 |
| `getOutOfStockProducts(User, int, int)` | readOnly | Products with stock = 0 |
| `adjustStock(Long, int, String, User)` | read-write | Updates stock with reason |

### Stock Adjustment Request

| Field | Type | Description |
|-------|------|-------------|
| `quantity` | int | New stock quantity |
| `reason` | String | Reason for adjustment |

---

## 9. Order Management

### SellerOrderController Endpoints

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `GET` | `/seller/orders` | `getOrders()` | `sellerOrderService.getOrders()` |
| `GET` | `/seller/orders/{id}` | `getOrderById()` | `sellerOrderService.getOrderById()` |
| `PUT` | `/seller/orders/{id}/status` | `updateOrderStatus()` | `sellerOrderService.updateOrderStatus()` |

### SellerOrderServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `getOrders(User)` | readOnly | Orders with seller's products |
| `getOrderById(Long, User)` | readOnly | Single order (filtered) |
| `updateOrderStatus(Long, OrderStatus, User)` | read-write | Status transition |

### SellerOrderResponse Fields

| Field | Type | Description |
|-------|------|-------------|
| `id`, `status`, `grandTotal`, `placedAt` | - | Order fields |
| `items` | List | Order items with snapshots |
