# Payment Gateway Service

> **Framework:** Spring Boot 4.1 + Strategy Pattern
> **Gateways:** Razorpay (implemented), Stripe (placeholder), PayPal (placeholder), UPI (placeholder), COD
> **Pattern:** Strategy Pattern — each gateway is a separate `PaymentGatewayStrategy` implementation

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Key Components](#2-key-components)
3. [Thymeleaf Templates](#3-thymeleaf-templates)
4. [Payment Lifecycle](#4-payment-lifecycle)
5. [Gateway Implementations](#5-gateway-implementations)
6. [REST API Endpoints](#6-rest-api-endpoints)

---

## 1. Architecture Overview

### Gateway Components

| Class | Path | Description |
|-------|------|-------------|
| `PaymentGatewayStrategy` | `gateway/payment/PaymentGatewayStrategy.java` | Strategy interface |
| `AbstractRestGateway` | `gateway/payment/AbstractRestGateway.java` | Base for HTTP gateways |
| `RazorpayGateway` | `gateway/payment/RazorpayGateway.java` | Razorpay implementation |
| `CodPaymentGateway` | `gateway/payment/CodPaymentGateway.java` | Cash on delivery |
| `PaymentGatewayRegistry` | `gateway/payment/PaymentGatewayRegistry.java` | Gateway selection |

### Service Layer

| Service | Path | Description |
|---------|------|-------------|
| `PaymentServiceImpl` | `service/impl/PaymentServiceImpl.java` | Payment orchestration |
| `UserPaymentController` | `controller/UserPaymentController.java` | User endpoints |
| `AdminPaymentController` | `controller/admin/AdminPaymentController.java` | Admin endpoints |

---

## 2. Key Components

### PaymentGatewayStrategy Methods

| Method | Description |
|--------|-------------|
| `supports()` | Returns PaymentGateway enum this strategy handles |
| `process(Payment)` | Creates payment record, initiates gateway processing |
| `verify(Payment, String)` | Confirms payment with gateway response |

### RazorpayGateway Methods

| Method | Description |
|--------|-------------|
| `process(Payment)` | POST `/v1/orders` with amount in paise, sets PROCESSING |
| `verify(Payment, String)` | GET `/v1/payments/{id}`, checks status = captured/authorized |

### AbstractRestGateway Methods

| Method | Description |
|--------|-------------|
| `exchange(HttpMethod, String, Object, Consumer)` | HTTP call with error handling |
| `parse(String)` | JSON parsing via Jackson |
| `toMinorUnits(BigDecimal)` | Converts to integer (paise for INR) |
| `basicAuth(user, secret)` | Basic auth header builder |
| `bearer(token)` | Bearer token header builder |

### PaymentServiceImpl Methods

| Method | Transaction | Description |
|--------|-----------|-------------|
| `createPayment(Order)` | read-write | Creates Payment, idempotent check, calls gateway strategy |
| `pay(User, Long)` | read-write | Validates ownership, initiates gateway payment |
| `verifyPayment(User, Long, String)` | read-write | Validates PROCESSING status, confirms with gateway |
| `refund(User, Long)` | read-write | Admin-only: PAID → REFUNDED |
| `cancelPayment(User, Long)` | read-write | PENDING/FAILED → CANCELLED |
| `getPayment(User, Long)` | readOnly | Returns payment details |

### Payment Reference Format

Format: `PAY-yyyyMMdd-{orderId}`
- Example: `PAY-20240115-42`

---

## 3. Thymeleaf Templates

| Template | URL | Controller Mapping | Purpose |
|----------|-----|-------------------|---------|
| `payment.html` | `/payment` | `FrontEndController.paymentPage()` | Payment form, gateway integration |
| `admin-payments.html` | `/admin/payments` | `FrontEndController.adminPaymentsPage()` | Admin payment management |

---

## 4. Payment Lifecycle

### Status Flow

```
createPayment() → PENDING
     │
     ├── [COD] → PENDING (no gateway call)
     │
     └── [Online] → PROCESSING (gateway call)
             │
             ├── verify → PAID
             │      │
             │      └── refund → REFUNDED
             │
             └── verify → FAILED
```

### Status Definitions

| Status | Description |
|--------|-------------|
| `PENDING` | Created at checkout, COD stays here |
| `PROCESSING` | Online payment initiated |
| `PAID` | Payment confirmed |
| `FAILED` | Gateway verification failed |
| `REFUNDED` | Admin-processed refund |
| `CANCELLED` | Cancelled before processing |

---

## 5. Gateway Implementations

### RazorpayGateway (Implemented)

| Aspect | Detail |
|--------|--------|
| Base URL | `https://api.razorpay.com/v1` |
| Auth | Basic Auth (api-key:api-secret) |
| Currency | INR only |
| Process | POST `/orders` with amount, currency, receipt |
| Verify | GET `/payments/{transactionId}` |
| Success States | `captured`, `authorized` |

### CodPaymentGateway

| Method | Behavior |
|--------|----------|
| `process(Payment)` | No-op: stays PENDING |
| `verify(Payment, String)` | No-op: no verification needed |

---

## 6. REST API Endpoints

### User Payments (`/user/payment`)

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `POST` | `/user/payment/pay/{id}` | `pay()` | `paymentService.pay()` |
| `POST` | `/user/payment/verify` | `verifyPayment()` | `paymentService.verifyPayment()` |
| `GET` | `/user/payment/{id}` | `getPayment()` | `paymentService.getPayment()` |

### Admin Payments (`/api/admin/payments`)

| Method | Endpoint | Controller Method | Service Method |
|--------|----------|-------------------|--------------|
| `GET` | `/api/admin/payments` | `getAllPayments()` | `adminPaymentService.getAllPayments()` |
| `GET` | `/api/admin/payments/{id}` | `getPaymentById()` | `adminPaymentService.getPaymentById()` |
| `PUT` | `/api/admin/payments/{id}/refund` | `refundPayment()` | `adminPaymentService.refundPayment()` |
| `GET` | `/api/admin/payments/stats` | `getPaymentStats()` | `adminPaymentService.getPaymentDashboardStats()` |

---

## 7. Configuration

### Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `RAZORPAY_KEY_ID` | Yes | Razorpay API key |
| `RAZORPAY_KEY_SECRET` | Yes | Razorpay API secret |
| `EMAIL_PROVIDER` | No | `smtp` or `console` (default: console) |

---

## 8. Error Handling

### Exceptions

| Exception | HTTP Status | When |
|-----------|-------------|------|
| `PaymentGatewayException` | 502 | Gateway API error |
| `PaymentInvalidStateException` | 409 | Invalid state transition |
| `PaymentNotFoundException` | 404 | Payment not found |
| `PaymentAccessDeniedException` | 403 | User doesn't own payment |
| `DuplicatePaymentException` | 409 | Duplicate payment attempt |
