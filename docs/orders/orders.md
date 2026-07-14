# Orders & Cart Service

> **Framework:** Spring Boot 4.1 + Spring Data JPA + Flyway
> **Entities:** Order, OrderItem, Cart, CartItem, OrderAddressSnapshot
> **Pattern:** Snapshot model for immutable order history

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Key Components](#2-key-components)
3. [Thymeleaf Templates](#3-thymeleaf-templates)
4. [Cart Management](#4-cart-management)
5. [Order Lifecycle](#5-order-lifecycle)
6. [Entity Model](#6-entity-model)
7. [REST API Endpoints](#7-rest-api-endpoints)
8. [Key Design Patterns](#8-key-design-patterns)

---

## 1. Architecture Overview

### Controller Layer

| Controller | Path | Endpoints | Description |
|------------|------|-----------|-------------|
| `UserCartController` | `controller/UserCartController.java` | 4 | Cart CRUD operations |
| `UserOrderController` | `controller/UserOrderController.java` | 7 | Checkout, order listing, actions |
| `UserPaymentController` | `controller/UserPaymentController.java` | 4 | Payment initiation, verification |
| `SellerOrderController` | `controller/seller/SellerOrderController.java` | 3 | Seller order view, status update |
| `AdminOrderController` | `controller/admin/AdminOrderController.java` | 3 | Admin order management |

### Service Layer

| Service | Path | Description |
|---------|------|-------------|
| `CartServiceImpl` | `service/impl/CartServiceImpl.java` | Cart entity management |
| `CheckoutServiceImpl` | `service/impl/CheckoutServiceImpl.java` | Checkout orchestration |
| `PaymentServiceImpl` | `service/impl/PaymentServiceImpl.java` | Payment gateway strategy |
| `OrderEmailService` | `service/OrderEmailService.java` | Order status email notifications |
| `SellerOrderServiceImpl` | `service/impl/SellerOrderServiceImpl.java` | Seller-specific orders |
| `AdminOrderServiceImpl` | `service/impl/AdminOrderServiceImpl.java` | Admin order operations |

---

## 2. Key Components

### UserCartController Endpoints

| Method | Endpoint | Service Method | Description |
|--------|----------|---------------|-------------|
| `POST` | `/user/cart/create` | `cartService.addItem()` | Add item to cart (creates if needed) |
| `GET` | `/user/cart/get` | `cartService.getCart()` | Get user's cart with items |
| `PUT` | `/user/cart/update/{id}` | `cartService.updateQuantity()` | Update cart item quantity |
| `DELETE` | `/user/cart/delete/{id}` | `cartService.removeItem()` | Remove item from cart |

### UserOrderController Endpoints

| Method | Endpoint | Service Method | Description |
|--------|----------|---------------|-------------|
| `POST` | `/user/order/checkout` | `checkoutService.checkout()` | Create order from cart |
| `GET` | `/user/order/get/all` | `orderService.getUserOrders()` | Paginated user orders |
| `GET` | `/user/order/get/{id}` | `orderService.getUserOrderById()` | Single order |
| `PUT` | `/user/order/cancel/{id}` | `orderService.cancelOrder()` | Cancel PLACED order |
| `PUT` | `/user/order/return/{id}` | `orderService.returnRequest()` | Request return |
| `PUT` | `/user/order/refund/{id}` | `orderService.refundRequest()` | Request refund |
| `PUT` | `/user/order/replace/{id}` | `orderService.replaceRequest()` | Request replacement |

### UserPaymentController Endpoints

| Method | Endpoint | Service Method | Description |
|--------|----------|---------------|-------------|
| `POST` | `/user/payment/pay/{id}` | `paymentService.pay()` | Initiate payment |
| `POST` | `/user/payment/verify` | `paymentService.verifyPayment()` | Verify gateway payment |
| `GET` | `/user/payment/{id}` | `paymentService.getPayment()` | Get payment details |

### SellerOrderController Endpoints

| Method | Endpoint | Service Method | Auth |
|--------|----------|---------------|------|
| `GET` | `/seller/orders` | `sellerOrderService.getOrders()` | SELLER/ADMIN |
| `GET` | `/seller/orders/{id}` | `sellerOrderService.getOrderById()` | SELLER/ADMIN |
| `PUT` | `/seller/orders/{id}/status` | `sellerOrderService.updateOrderStatus()` | SELLER/ADMIN |

### AdminOrderController Endpoints

| Method | Endpoint | Service Method |
|--------|----------|---------------|
| `GET` | `/api/admin/orders` | `adminOrderService.getAllOrders()` |
| `GET` | `/api/admin/orders/{id}` | `adminOrderService.getOrderById()` |
| `PUT` | `/api/admin/orders/{id}/status` | `adminOrderService.updateOrderStatus()` |

---

## 3. Thymeleaf Templates

| Template | URL | Controller Mapping | Purpose |
|----------|-----|-------------------|---------|
| `cart.html` | `/cart` | `FrontEndController.cartPage()` | Shopping cart view |
| `checkout.html` | `/checkout` | `FrontEndController.checkoutPage()` | Checkout form |
| `orders.html` | `/orders` | `FrontEndController.ordersPage()` | User order history |
| `order-detail.html` | `/order-detail` | `FrontEndController.orderDetailPage()` | Order details |
| `payment.html` | `/payment` | `FrontEndController.paymentPage()` | Payment processing |
| `seller/orders.html` | `/seller-panel/orders` | `FrontEndController.sellerOrdersPage()` | Seller order listing |
| `seller/order-detail.html` | `/seller-panel/order-detail` | `FrontEndController.sellerOrderDetailPage()` | Seller order details |
| `admin-orders.html` | `/admin/orders` | `FrontEndController.adminOrdersPage()` | Admin order management |

---

## 2. Cart Management

### Cart Entity

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `user` | User | `NOT NULL`, unique FK | One-to-one owner |
| `items` | List\<CartItem\> | cascade ALL | One-to-many line items |

### CartItem Entity

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `cart` | Cart | `NOT NULL`, FK | Parent cart |
| `item` | Item | `NOT NULL`, FK | Product reference |
| `quantity` | int | `NOT NULL`, min 1 | Units ordered |

### Cart Behaviors

| Behavior | Description |
|----------|-------------|
| Auto-creation | Cart silently created if user has none |
| Duplicate merging | Adding existing product increments quantity |
| Idempotent add | Same product → quantity increase, not duplicate line |
| Empty safety | `get()` returns valid empty structure |
| Ownership enforcement | All operations scoped to authenticated user |
| Unique constraint | One cart per user enforced at DB level |

---

## 3. Order Lifecycle

### Status Flow

```
┌──────────────────────────────────────────────────────────────┐
│                     ORDER STATUS                             │
│                                                              │
│  PLACED ──→ PROCESSING ──→ SHIPPED ──→ DELIVERED             │
│    │                          │            │                 │
│    ↓                          ↓            ↓                 │
│  CANCELLED               CANCELLED   RETURN_REQUESTED        │
│                                      REFUND_REQUESTED        │
│                                      REPLACE_REQUESTED       │
│                                            │                 │
│                                            ↓                 │
│                                      RETURNED/REFUNDED/      │
│                                      REPLACED                │
└──────────────────────────────────────────────────────────────┘
```

### Allowed Transitions

| Current Status | Allowed Next | Controller |
|---------------|--------------|------------|
| PLACED | CANCELLED | `UserOrderController.cancel()` |
| PLACED | PROCESSING, SHIPPED, DELIVERED, CANCELLED | `AdminOrderController` |
| PROCESSING | SHIPPED, DELIVERED, CANCELLED | `AdminOrderController` |
| SHIPPED | DELIVERED, CANCELLED | `AdminOrderController` |
| DELIVERED | RETURN_REQUESTED | `UserOrderController.returnRequest()` |
| DELIVERED | REFUND_REQUESTED | `UserOrderController.refundRequest()` |
| DELIVERED | REPLACE_REQUESTED | `UserOrderController.replaceRequest()` |

### Payment Status

| Status | Description |
|--------|-------------|
| `PENDING` | Payment created, awaiting initiation |
| `PROCESSING` | Online payment initiated |
| `PAID` | Payment confirmed |
| `FAILED` | Payment verification failed |
| `REFUNDED` | Admin-processed refund |
| `CANCELLED` | Payment cancelled |

---

## 4. Entity Model

### Order Entity

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `user` | User | `NOT NULL`, lazy FK | Customer |
| `address` | Address | lazy, nullable FK | Original address ref |
| `shippingAddress` | OrderAddressSnapshot | Embedded | Immutable snapshot |
| `status` | OrderStatus | `NOT NULL`, STRING | Lifecycle status |
| `paymentMethod` | PaymentMethod | `NOT NULL`, STRING | COD, RAZORPAY, etc. |
| `paymentStatus` | PaymentStatus | `NOT NULL`, STRING | PENDING, PAID, etc. |
| `subtotal` | BigDecimal | precision 12, scale 2 | Sum of line items |
| `shippingFee` | BigDecimal | precision 10, scale 2 | Shipping cost |
| `tax` | BigDecimal | precision 10, scale 2 | Tax amount |
| `discount` | BigDecimal | precision 10, scale 2 | Promo discount |
| `grandTotal` | BigDecimal | precision 12, scale 2 | Final total |
| `promoCode` | PromoCode | lazy, nullable FK | Historical reference |
| `placedAt` | Instant | `NOT NULL` | Order timestamp |
| `orderItems` | List\<OrderItem\> | cascade ALL | Line items |

### OrderItem Entity (Snapshot Model)

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `order` | Order | `NOT NULL`, FK | Parent order |
| `item` | Item | lazy, nullable FK | Product reference |
| `itemName` | String | `NOT NULL` | **Snapshotted** product name |
| `itemSlug` | String | `NOT NULL` | **Snapshotted** product slug |
| `unitPrice` | BigDecimal | `NOT NULL` | **Snapshotted** price at purchase |
| `quantity` | int | `NOT NULL` | Units ordered |
| `totalPrice` | BigDecimal | `NOT NULL` | unitPrice × quantity |

### OrderAddressSnapshot (Embedded)

| Field | Type | Notes |
|-------|------|-------|
| `fullName` | String | Snapshot of address at checkout |
| `addressLine1` | String | Street address |
| `addressLine2` | String | Apartment, suite, etc. |
| `city` | String | City |
| `state` | String | State/Province |
| `postalCode` | String | ZIP/Postal code |
| `country` | String | Country |
| `phoneNumber` | String | Contact number |

### Entity Relationships

```
User (1) ──── (*) Order ──── (*) OrderItem
  │               │               │
  │               │               │ (snapshot)
  │               │               v
  │               │           Item ──── (*) ItemReview
  │               │               │
  │               │               │ (N:1)
  │               │               v
  │               │           Category
  │               │
  │               ├── Address (nullable FK)
  │               │
  │               └── PromoCode (nullable FK)
  │
  └── Cart (1:1) ──── (*) CartItem ──── (*) Item
```

---

## 7. REST API Endpoints

### User Cart (`/user/cart`)

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `POST` | `/user/cart/create` | `createItem()` | `cartService.addItem()` |
| `GET` | `/user/cart/get` | `getCart()` | `cartService.getCart()` |
| `PUT` | `/user/cart/update/{id}` | `updateQuantity()` | `cartService.updateQuantity()` |
| `DELETE` | `/user/cart/delete/{id}` | `deleteItem()` | `cartService.removeItem()` |

### User Orders (`/user/order`)

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `POST` | `/user/order/checkout` | `checkout()` | `checkoutService.checkout()` |
| `GET` | `/user/order/get/all` | `getUserOrders()` | `orderService.getUserOrders()` |
| `GET` | `/user/order/get/{id}` | `getUserOrderById()` | `orderService.getUserOrderById()` |
| `PUT` | `/user/order/cancel/{id}` | `cancelOrder()` | `orderService.cancelOrder()` |
| `PUT` | `/user/order/return/{id}` | `returnRequest()` | `orderService.returnRequest()` |
| `PUT` | `/user/order/refund/{id}` | `refundRequest()` | `orderService.refundRequest()` |
| `PUT` | `/user/order/replace/{id}` | `replaceRequest()` | `orderService.replaceRequest()` |

### Admin Orders (`/api/admin/orders`)

| Method | Endpoint | Controller Method | Description |
|--------|----------|-------------------|-------------|
| `GET` | `/api/admin/orders` | `getAllOrders()` | Paginated, filterable |
| `GET` | `/api/admin/orders/{id}` | `getOrderById()` | Single order |
| `PUT` | `/api/admin/orders/{id}/status` | `updateOrderStatus()` | Status transition |

### Seller Orders (`/seller/orders`)

| Method | Endpoint | Controller Method |
|--------|----------|-------------------|
| `GET` | `/seller/orders` | `getOrders()` |
| `GET` | `/seller/orders/{id}` | `getOrderById()` |
| `PUT` | `/seller/orders/{id}/status` | `updateOrderStatus()` |

---

## 8. Key Design Patterns

### Snapshot Model

OrderItem captures product data at purchase time:
- **Why**: Historical orders remain accurate even if products change or are deleted
- **Fields snapshotted**: `itemName`, `itemSlug`, `unitPrice`
- **Original item**: Referenced via nullable FK (may be deleted later)

### Dual Address Strategy

| Strategy | Field | Purpose |
|----------|-------|---------|
| Referential | `address` (FK) | Preserves link to original address |
| Snapshot | `shippingAddress` (embedded) | Guarantees immutability |

### Ownership Enforcement

All user-scoped endpoints:
1. Resolve principal from `@AuthenticationPrincipal User user`
2. Validate ownership at service layer
3. Never accept user ID from client-supplied data

### Pagination with Caps

All paginated endpoints clamp `size` via `PaginationProperties`:
```java
int safeSize = Math.min(size, pagination.maxPageSize());
```

### Checkout Flow

```
1. Validate cart not empty
2. For each cart item:
   a. Load item with stock info
   b. Validate stock >= quantity
   c. Deduct stock (optimistic locking)
   d. Create OrderItem snapshot
3. Calculate totals: subtotal, shipping, tax, discount
4. Create Order with PENDING payment status
5. Create Payment record
6. Clear cart
7. Return CheckoutResponse (orderId, grandTotal, paymentId)
```
