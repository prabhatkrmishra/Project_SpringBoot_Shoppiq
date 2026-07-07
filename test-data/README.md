# Shoppiq Test Data

This directory contains JSON test data for populating the Shoppiq database via the REST API endpoints.

## Bulk (list) support via Admin — IMPORTANT

All 8 entities now support **bulk ingestion through admin**:

| Entity | Bulk via Admin? | Endpoint |
|--------|----------------|----------|
| Categories | ✅ Yes | `POST /api/admin/test/categories/bulk` |
| Items | ✅ Yes | `POST /api/admin/test/items/bulk` |
| Users | ✅ Yes | `POST /api/admin/test/users/bulk` |
| Addresses | ✅ Yes | `POST /api/admin/test/addresses/bulk` |
| Reviews | ✅ Yes | `POST /api/admin/test/reviews/bulk` |
| Sellers | ✅ Yes | `POST /api/admin/test/sellers/bulk` |
| Carts | ✅ Yes | `POST /api/admin/test/carts/bulk` |
| Orders | ✅ Yes | `POST /api/admin/test/orders/bulk` |

> All bulk endpoints require `ADMIN` role. User context is supplied inline
> in the request body (userId field per item) rather than extracted from
> the security context.

## Existing Categories (already seeded, IDs 1–20)

The database is pre-seeded with these 20 categories, so **do not re-create them**:
`1:Electronics`, `2:Fashion`, `3:Home & Kitchen`, `4:Books`, `5:Grocery`,
`6:Beauty & Personal Care`, `7:Health`, `8:Sports & Outdoors`, `9:Toys & Games`,
`10:Baby Products`, `11:Automotive`, `12:Office Supplies`, `13:Pet Supplies`,
`14:Garden & Outdoor`, `15:Jewellery`, `16:Footwear`, `17:Bags & Luggage`,
`18:Furniture`, `19:Musical Instruments`, `20:Software`.

- `categories.json` contains **40 NEW** categories (no name conflicts) → safe to bulk-import.
  After import the catalog will have 20 (seeded) + 40 (new) = 60 categories.
- `items.json` category IDs were remapped to the **existing 1–20** range (bulk item
  creation throws `CategoryNotFoundException` if any referenced category is missing).

## Files

| File | Endpoint | Count | Bulk? | Auth | `userId` inline? | Structure |
|------|----------|-------|-------|------|-----------------|-----------|
| `categories.json` | `POST /api/admin/test/categories/bulk` | 40 (new) | ✅ | Admin | — | `{"categories": [...]}` |
| `items.json` | `POST /api/admin/test/items/bulk` | 59 | ✅ | Admin | — | `{"items": [...]}` |
| `users.json` | `POST /api/admin/test/users/bulk` | 55 | ✅ | Admin | — | `{"users": [...]}` |
| `addresses.json` | `POST /api/admin/test/addresses/bulk` | 60 | ✅ | Admin | ✅ | `{"addresses": [{"userId": N, "address": {...}}]}` |
| `reviews.json` | `POST /api/admin/test/reviews/bulk` | 118 | ✅ | Admin | ✅ | `{"reviews": [{"userId": N, "itemId": N, "rating": N, "review": "..."}]}` |
| `sellers.json` | `POST /api/admin/test/sellers/bulk` | 52 | ✅ | Admin | ✅ | `{"sellers": [{"userId": N, "seller": {...}}]}` |
| `carts.json` | `POST /api/admin/test/carts/bulk` | 59 | ✅ | Admin | ✅ | `{"cartItems": [{"userId": N, "itemDetailsId": N, "quantity": N}]}` |
| `orders.json` | `POST /api/admin/test/orders/bulk` | 55 | ✅ | Admin | ✅ | `{"orders": [{"userId": N, "addressId": N, "paymentMethod": "..."}]}` |

## Prerequisites

1. Start the application (Spring Boot server on `http://localhost:8080`)
2. Register an admin user
3. Log in to get JWT cookie (all bulk operations use the same admin session)

## Quick Start

### 1. Register an admin user
```bash
curl -X POST http://localhost:8080/user/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin User","email":"admin@shoppiq.com","username":"admin","password":"Admin123!"}'
```

### 2. Login and save cookies
```bash
curl -c cookies.txt -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123!"}'
```

### 3. Load categories (Admin)
```bash
curl -b cookies.txt -X POST http://localhost:8080/api/admin/test/categories/bulk \
  -H "Content-Type: application/json" \
  -d @categories.json
```

### 4. Load items (Admin)
```bash
curl -b cookies.txt -X POST http://localhost:8080/api/admin/test/items/bulk \
  -H "Content-Type: application/json" \
  -d @items.json
```

### 5. Load users (Admin)
```bash
curl -b cookies.txt -X POST http://localhost:8080/api/admin/test/users/bulk \
  -H "Content-Type: application/json" \
  -d @users.json
```

### 6. Load addresses (Admin)
```bash
curl -b cookies.txt -X POST http://localhost:8080/api/admin/test/addresses/bulk \
  -H "Content-Type: application/json" \
  -d @addresses.json
```

### 7. Load reviews (Admin)
```bash
curl -b cookies.txt -X POST http://localhost:8080/api/admin/test/reviews/bulk \
  -H "Content-Type: application/json" \
  -d @reviews.json
```

### 8. Register sellers (Admin)
```bash
curl -b cookies.txt -X POST http://localhost:8080/api/admin/test/sellers/bulk \
  -H "Content-Type: application/json" \
  -d @sellers.json
```

### 9. Add to cart (Admin)
```bash
curl -b cookies.txt -X POST http://localhost:8080/api/admin/test/carts/bulk \
  -H "Content-Type: application/json" \
  -d @carts.json
```

### 10. Place orders (Admin)
```bash
curl -b cookies.txt -X POST http://localhost:8080/api/admin/test/orders/bulk \
  -H "Content-Type: application/json" \
  -d @orders.json
```

## Notes

- All test-data endpoints require a valid admin JWT cookie (`credentials: include`)
- The bulk users endpoint creates users via `POST /api/admin/test/users/bulk` — they are assigned the `ROLE_CUSTOMER` role by default
- **`userId` is supplied inline per item** in the request body (not from `AuthenticationPrincipal`). The `userId` values in these files assume the users are created in order (IDs 1-55). If your user IDs differ, adjust the JSON accordingly.
- `addresses.json` uses a nested `{"userId": N, "address": {...}}` structure where the inner address matches the standard `CreateAddressRequest` schema (label, fullName, phone, line1, line2, city, state, postalCode, country, default). Addresses are assigned to users by name mapping.
- `sellers.json` uses a nested `{"userId": N, "seller": {...}}` structure where the inner seller matches the standard `SellerRegistrationRequest` schema (businessName, businessEmail, phone, gstNumber, panNumber).
- `carts.json` uses the wrapper key `cartItems` (not `carts`) — each entry adds an item to the specified user's cart.
- Item IDs in `reviews.json` reference the item IDs returned by the items endpoint
- Address IDs in `orders.json` reference the address IDs created via the address endpoint
- The default password for all test users is `Password123!`
- **Load order matters**: create users first, then addresses/reviews/sellers, then carts, then orders (orders require cart items)

## Category ID Reference (seeded categories used by items.json)

These are the **existing** categories (IDs 1–20) that `items.json` references:

| ID | Category | ID | Category |
|----|----------|----|----------|
| 1 | Electronics | 11 | Automotive |
| 2 | Fashion | 12 | Office Supplies |
| 3 | Home & Kitchen | 13 | Pet Supplies |
| 4 | Books | 14 | Garden & Outdoor |
| 5 | Grocery | 15 | Jewellery |
| 6 | Beauty & Personal Care | 16 | Footwear |
| 7 | Health | 17 | Bags & Luggage |
| 8 | Sports & Outdoors | 18 | Furniture |
| 9 | Toys & Games | 19 | Musical Instruments |
| 10 | Baby Products | 20 | Software |

> The 40 new categories in `categories.json` will be created with IDs **21+** by the
> database. They are not referenced by `items.json` (which only uses the seeded 1–20).
> If you want items to use a newly-created category, note its returned ID after import.
