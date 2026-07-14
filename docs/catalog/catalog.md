# Product Catalog Service

> **Framework:** Spring Boot 4.1 + Spring Data JPA + Flyway
> **Entities:** Item, ItemDetails, Category, ItemReview
> **Pattern:** Read-only public API + Admin/Seller write endpoints

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Key Components](#2-key-components)
3. [Thymeleaf Templates](#3-thymeleaf-templates)
4. [Entity Model](#4-entity-model)
5. [REST API Endpoints](#5-rest-api-endpoints)
6. [Item Management](#6-item-management)
7. [Category Management](#7-category-management)
8. [Reviews System](#8-reviews-system)
9. [Search & Filtering](#9-search--filtering)

---

## 1. Architecture Overview

### Controller Layer

| Controller | Path | Endpoints | Description |
|------------|------|-----------|-------------|
| `ItemController` | `controller/ItemController.java` | 7 | Public product browsing |
| `CategoryController` | `controller/CategoryController.java` | 7 | Category browsing and admin CRUD |
| `ItemReviewController` | `controller/ItemReviewController.java` | 3 | Public review listing, user posting |
| `SellerProductController` | `controller/seller/SellerProductController.java` | 5 | Seller product CRUD |
| `AdminProductController` | `controller/admin/AdminProductController.java` | 3 | Admin product approval/rejection |

### Service Layer

| Service | Path | Description |
|---------|------|-------------|
| `ItemServiceImpl` | `service/impl/ItemServiceImpl.java` | Public item queries, top-selling logic |
| `CategoryServiceImpl` | `service/impl/CategoryServiceImpl.java` | Category CRUD, top-selling categories |
| `ItemReviewServiceImpl` | `service/impl/ItemReviewServiceImpl.java` | Review CRUD, status workflow |
| `SellerProductServiceImpl` | `service/impl/SellerProductServiceImpl.java` | Seller product creation/update/delete |
| `AdminProductServiceImpl` | `service/impl/AdminProductServiceImpl.java` | Admin product approval/rejection |

---

## 2. Key Components

### ItemController Endpoints

| Method | Endpoint | Service Method | Response DTO |
|--------|----------|---------------|--------------|
| `GET` | `/items/all` | `itemService.getAll()` | `PageResponse<ItemResponse>` |
| `GET` | `/items/{id}` | `itemService.getById()` | `ItemResponse` |
| `GET` | `/items/slug/{slug}` | `itemService.getBySlug()` | `ItemResponse` |
| `GET` | `/items/new-arrivals` | `itemService.getNewArrivals()` | `PageResponse<ItemResponse>` |
| `GET` | `/items/sale` | `itemService.getSaleItems()` | `PageResponse<ItemResponse>` |
| `GET` | `/items/category/{slug}` | `itemService.getByCategorySlug()` | `PageResponse<ItemResponse>` |
| `GET` | `/items/top-selling` | `itemService.getTopSelling()` | `List<ItemResponse>` |

### CategoryController Endpoints

| Method | Endpoint | Service Method | Response DTO |
|--------|----------|---------------|-------------|
| `GET` | `/categories/all` | `categoryService.getAll()` | `List<CategoryResponse>` |
| `GET` | `/categories/all/paged` | `categoryService.getAll()` | `PageResponse<CategoryResponse>` |
| `GET` | `/categories/slug/{slug}` | `categoryService.getBySlug()` | `CategoryResponse` |
| `GET` | `/categories/top-selling` | `categoryService.getTopSelling()` | `List<CategoryResponse>` |
| `POST` | `/categories` | `categoryService.create()` | `CategoryResponse` (201) |
| `PUT` | `/categories/{id}/update` | `categoryService.update()` | `CategoryResponse` |
| `DELETE` | `/categories/{id}/delete` | `categoryService.delete()` | void (204) |

### SellerProductController Endpoints

| Method | Endpoint | Service Method | Auth |
|--------|----------|---------------|------|
| `POST` | `/seller/products/create` | `sellerProductService.createProduct()` | SELLER/ADMIN |
| `GET` | `/seller/products` | `sellerProductService.getMyProducts()` | SELLER/ADMIN |
| `GET` | `/seller/products/{id}` | `sellerProductService.getMyProductById()` | SELLER/ADMIN |
| `PUT` | `/seller/products/update/{id}` | `sellerProductService.updateProduct()` | SELLER/ADMIN |
| `DELETE` | `/seller/products/delete/{id}` | `sellerProductService.deleteProduct()` | SELLER/ADMIN |

---

## 3. Thymeleaf Templates

| Template | URL | Controller Mapping | Purpose |
|----------|-----|-------------------|---------|
| `allitems.html` | `/allitems`, `/shop` | `FrontEndController.itemsPage()` | Product listing page |
| `item-detail.html` | `/item/{slug}` | `FrontEndController.itemDetailPage()` | Single product page |
| `new-arrivals.html` | `/new-arrivals` | `FrontEndController.newArrivalsPage()` | New arrivals page |
| `sale.html` | `/sale` | `FrontEndController.salePage()` | On-sale products |
| `categories.html` | `/categories` | `FrontEndController.categoriesPage()` | Category listing |
| `category.html` | `/category/{slug}` | `FrontEndController.categoryPage()` | Category products |
| `admin-products.html` | `/admin/products` | `FrontEndController.adminProductsPage()` | Admin product queue |
| `admin-categories.html` | `/admin/categories` | `FrontEndController.adminCategoriesPage()` | Admin category management |
| `admin-sale.html` | `/admin/sale` | `FrontEndController.adminSalePage()` | Sale management |

---

## 4. Entity Model

### Item Entity

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `name` | String | `NOT NULL`, max 150 | Product display name |
| `slug` | String | `NOT NULL`, max 200, `UNIQUE` | URL-friendly identifier |
| `description` | String | `NOT NULL`, max 500 | Short product description |
| `seller` | Seller | Lazy, nullable | FK `seller_id` |
| `publishingStatus` | ProductPublishingStatus | `NOT NULL`, default `DRAFT` | DRAFT, PENDING, PUBLISHED |
| `itemDetails` | ItemDetails | `NOT NULL`, cascade ALL | One-to-one commercial data |
| `itemReviews` | List\<ItemReview\> | cascade ALL | One-to-many reviews |

### ItemDetails Entity

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `price` | BigDecimal | `NOT NULL`, precision 12, scale 2 | Current selling price |
| `discountPercent` | BigDecimal | precision 5, scale 2 | Discount percentage |
| `onSale` | boolean | default `false` | Sale flag |
| `stockQuantity` | int | `NOT NULL` | Available inventory |
| `lowStockThreshold` | int | default `10` | Low stock alert level |
| `category` | Category | Lazy, nullable | FK category |

### Category Entity

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `name` | String | `NOT NULL`, max 100 | Category display name |
| `slug` | String | `NOT NULL`, max 120, `UNIQUE` | URL-friendly identifier |
| `description` | String | max 500 | Optional description |
| `image` | String | max 255 | Image URL |

### Entity Relationships

```
Category (1) ──── (*) ItemDetails (*) ──── (1) Item
                                                │
                                                │ (N:1)
                                                v
                                              Seller
                                                │
                                                │ (N:1)
                                                v
                                            User (owner)

Item (*) ──── (*) ItemReview (*) ──── (1) User (reviewer)
```

---

## 5. REST API Endpoints

### Public Browsing (`/items`)

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `GET` | `/items/all` | `getAll()` | `itemService.getAll()` |
| `GET` | `/items/{id}` | `getById()` | `itemService.getById()` |
| `GET` | `/items/slug/{slug}` | `getBySlug()` | `itemService.getBySlug()` |
| `GET` | `/items/new-arrivals` | `getNewArrivals()` | `itemService.getNewArrivals()` |
| `GET` | `/items/sale` | `getSaleItems()` | `itemService.getSaleItems()` |
| `GET` | `/items/category/{slug}` | `getByCategorySlug()` | `itemService.getByCategorySlug()` |
| `GET` | `/items/top-selling` | `getTopSelling()` | `itemService.getTopSelling()` |

### Category Browsing (`/categories`)

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `GET` | `/categories/all` | `getAll()` | `categoryService.getAll()` |
| `GET` | `/categories/all/paged` | `getAllPaginated()` | `categoryService.getAll()` |
| `GET` | `/categories/slug/{slug}` | `getBySlug()` | `categoryService.getBySlug()` |
| `GET` | `/categories/top-selling` | `getTopSelling()` | `categoryService.getTopSelling()` |

### Seller Products (`/seller/products`)

| Method | Endpoint | Controller Method | Service Method | Auth |
|--------|----------|-------------------|--------------|------|
| `POST` | `/seller/products/create` | `createProduct()` | `sellerProductService.createProduct()` | SELLER/ADMIN |
| `GET` | `/seller/products` | `getMyProducts()` | `sellerProductService.getMyProducts()` | SELLER/ADMIN |
| `GET` | `/seller/products/{id}` | `getMyProductById()` | `sellerProductService.getMyProductById()` | SELLER/ADMIN |
| `PUT` | `/seller/products/update/{id}` | `updateProduct()` | `sellerProductService.updateProduct()` | SELLER/ADMIN |
| `DELETE` | `/seller/products/delete/{id}` | `deleteProduct()` | `sellerProductService.deleteProduct()` | SELLER/ADMIN |

### Admin Products (`/api/admin/products`)

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `GET` | `/api/admin/products/pending` | `getPendingProducts()` | `adminProductService.getPendingProducts()` |
| `PUT` | `/api/admin/products/{id}/publish` | `publishProduct()` | `adminProductService.publishProduct()` |
| `PUT` | `/api/admin/products/{id}/reject` | `rejectProduct()` | `adminProductService.rejectProduct()` |

---

## 6. Item Management

### ProductPublishingStatus

| Status | Description |
|--------|-------------|
| `DRAFT` | Seller is editing, not submitted |
| `PENDING` | Submitted, awaiting admin approval |
| `PUBLISHED` | Live and visible to customers |

### ItemServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `createBulk(List<ItemRequest>)` | read-write | Admin/test data creation |
| `getById(Long)` | readOnly | Single product by ID |
| `getBySlug(String)` | readOnly | Single product by slug |
| `getAll(int, int)` | readOnly | Paginated product listing |
| `getNewArrivals(int, int)` | readOnly | Products ordered by createdAt DESC |
| `getSaleItems(int, int)` | readOnly | Products where onSale=true |
| `getByCategorySlug(String, int, int)` | readOnly | Products filtered by category |
| `getTopSelling(int)` | readOnly | Top 8 by last 30 days delivered orders |

---

## 7. Category Management

### CategoryServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `create(CategoryRequest)` | read-write | Creates category, validates slug/name uniqueness |
| `update(Long, CategoryRequest)` | read-write | Updates mutable fields |
| `delete(Long)` | read-write | Deletes category |
| `getAll()` | readOnly | All categories |
| `getAll(int, int, String)` | readOnly | Paginated with optional search |
| `getBySlug(String)` | readOnly | Single category by slug |
| `getTopSelling(int)` | readOnly | Top categories by order count |

### Slug Validation

Regex: `^[a-z0-9]+(?:-[a-z0-9]+)*$`
- Max length: 120 characters
- Examples: `electronics`, `home-garden`, `fashion-men`

---

## 8. Reviews System

### ItemReviewController Endpoints

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `GET` | `/reviews/{itemId}` | `getReviewsByItemId()` | `itemReviewService.getReviewsByItemId()` |
| `POST` | `/reviews/{itemId}` | `createReview()` | `itemReviewService.createReview()` |
| `DELETE` | `/reviews/{reviewId}` | `deleteReview()` | `itemReviewService.deleteReview()` |

### ItemReviewServiceImpl Methods

| Method | Transaction | Description |
|--------|-------------|-------------|
| `getReviewsByItemId(Long, int, int)` | readOnly | Paginated reviews for product |
| `createReview(Long, ItemReviewRequest, User)` | read-write | Creates review, status=PENDING |
| `deleteReview(Long, User)` | read-write | Deletes own review |

### ReviewStatus

| Status | Description |
|--------|-------------|
| `PENDING` | Awaiting admin approval |
| `APPROVED` | Visible on product page |
| `REJECTED` | Not visible, admin decision |

---
