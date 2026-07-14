# Security & Infrastructure

> **Framework:** Spring Security 7.1 + Bucket4j + Docker
> **Pattern:** Defense in depth — multiple security layers
> **Rate Limiting:** Token bucket algorithm with configurable rules

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Key Components](#2-key-components)
3. [Thymeleaf Templates](#3-thymeleaf-templates)
4. [Rate Limiting](#4-rate-limiting)
5. [CSRF Protection](#5-csrf-protection)
6. [Cookie Security](#6-cookie-security)
7. [Error Handling](#7-error-handling)
8. [Docker Infrastructure](#8-docker-infrastructure)

---

## 1. Architecture Overview

### Defense Layers

```
Layer 1: Rate Limiting (Bucket4j)
  ├───── IP-based for unauthenticated
  └───── User+IP for authenticated critical endpoints

Layer 2: OAuth2 Return URL Filter
  └───── Validates returnUrl prevents open redirect

Layer 3: JWT Authentication Filter
  ├───── Extracts JWT from HttpOnly cookie
  ├───── Validates signature, expiry, version
  └───── Loads user from database (single query)

Layer 4: Spring Security Authorization
  ├───── URL-based authorization rules
  └───── Method-level @PreAuthorize

Layer 5: CSRF Protection (SPA mode)
  └───── X-XSRF-TOKEN header for state-changing requests

Layer 6: Cookie Security
  ├───── HttpOnly (blocks JavaScript access)
  ├───── Secure (HTTPS-only in production)
  └───── SameSite (Lax/Strict based on use case)
```

---

## 2. Key Components

### SecurityConfig

| Method/Config | Description |
|---------------|-------------|
| `securityFilterChain()` | Configures all security filters |
| `corsConfigurationSource()` | CORS from CorsProperties |
| `csrf()` | SPA mode, selective exemptions |
| `sessionManagement()` | Stateless (no sessions) |

### RateLimitFilter

| Method | Description |
|--------|-------------|
| `doFilterInternal()` | Checks rate limit for each request |
| `isExempt()` | Bypasses exempt endpoints |
| `getRateLimitKey()` | Generates IP or user:IP key |

### RateLimitProperties

| Field | Default | Description |
|-------|---------|-------------|
| `enabled` | true | Master switch |
| `rules` | List | Path-based configuration |

### Rate Limit Rules

| Endpoint | Limit | Duration | Key Type |
|----------|-------|----------|----------|
| `/auth/login` | 10 | 15 min | IP |
| `/auth/forgot-password` | 10 | 1 hour | IP |
| `/auth/refresh` | 10 | 15 min | IP |
| `/user/order/checkout` | 10 | 1 min | USER_IP |
| `/user/payment/pay/*` | 10 | 1 min | USER_IP |
| `/contact` | 10 | 1 hour | IP |

---

## 3. Thymeleaf Templates

| Template | URL | Controller Mapping | Purpose |
|----------|-----|-------------------|---------|
| `error.html` | `/error` | `FrontEndController` (implicit) | Error display page |

---

## 2. Rate Limiting

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  RateLimitFilter (extends OncePerRequestFilter)             │
│  ├── Placed BEFORE JwtAuthenticationFilter                  │
│  ├── Uses Bucket4j token-bucket algorithm                   │
│  ├── Bucket storage: ConcurrentHashMap (max 10,000)         │
│  └── Stale bucket eviction every 300 seconds                │
│                                                             │
│  RateLimitProperties                                        │
│  ├── enabled: boolean (master switch)                       │
│  └── rules: List<Rule> (path-based configuration)           │
│                                                             │
│  KeyType:                                                   │
│  ├── IP: remote address only                                │
│  └── USER_IP: composite userId:ip                           │
└─────────────────────────────────────────────────────────────┘
```

### Default Rules

| Endpoint | Limit | Duration | Key Type |
|----------|-------|----------|----------|
| `/auth/login` | 10 requests | 15 min | IP |
| `/user/register` | 10 requests | 1 hour | IP |
| `/auth/forgot-password` | 10 requests | 1 hour | IP |
| `/auth/reset-password` | 10 requests | 1 hour | IP |
| `/auth/verify-email` | 10 requests | 1 hour | IP |
| `/auth/confirm-email` | 10 requests | 1 hour | IP |
| `/auth/refresh` | 10 requests | 15 min | IP |
| `/auth/google/complete-profile` | 10 requests | 1 hour | IP |
| `/contact` | 10 requests | 1 hour | IP |
| `/api/newsletter/subscribe` | 10 requests | 1 hour | IP |
| `/user/order/checkout` | 10 requests | 1 min | USER_IP |
| `/user/payment/pay/*` | 10 requests | 1 min | USER_IP |
| `/user/payment/verify` | 10 requests | 1 min | USER_IP |
| `/user/password` | 10 requests | 1 min | USER_IP |

### Rate Exceeded Response

```json
{
  "type": "urn:problem-type:too-many-requests",
  "title": "Too Many Requests",
  "status": 429,
  "detail": "Rate limit exceeded. Please try again in 45 seconds.",
  "instance": "/auth/login"
}
```

Headers:
- `Retry-After: 45` (seconds)
- `X-Rate-Limit-Retry-After-Seconds: 45`

### Bucket Lifecycle

| Aspect | Value |
|--------|-------|
| Max buckets | 10,000 |
| Stale eviction | Every 300 seconds |
| Bucket age threshold | 3600 seconds |
| Idle threshold | 1800 seconds |
| Full map behavior | Force eviction → zero-capacity bucket |

---

## 3. CSRF Protection

### Configuration

```java
.csrf(csrf -> csrf
    .spa()
    .ignoringRequestMatchers(
        "/auth/login", "/auth/logout", "/auth/refresh",
        "/auth/forgot-password", "/auth/reset-password",
        "/auth/google/**", "/user/register",
        "/api/newsletter/**", "/api/banners/active",
        "/api/ai/guest/**", "/api/ai/chat"
    )
)
```

### SPA Mode

- Uses Spring Security's SPA-aware CSRF protection
- Frontend must include `X-XSRF-TOKEN` header for state-changing requests
- Token read from `XSRF-TOKEN` cookie by JavaScript

### Exemption Rationale

| Exempt Endpoint | Reason |
|----------------|--------|
| Auth endpoints | Public, no session to forge |
| OAuth2 endpoints | Protected by OAuth2 state parameter |
| Newsletter/banners | GET-safe or public |
| Guest AI endpoints | No authentication session |
| Chat creation | No CSRF needed for new conversation |

---

## 4. CORS Configuration

### Default Configuration

```yaml
app:
  cors:
    enabled: false                    # Production: true
    allowed-origins: http://localhost:3000
    allowed-methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
    allowed-headers: Authorization, Content-Type, X-Requested-With, Accept, Origin, Cache-Control, X-Request-Id
    exposed-headers: X-Request-Id
    allow-credentials: true
    max-age: 3600
```

### Production Recommendations

- Enable CORS for separate frontend/microservice deployments
- Use specific allowed-origins (not wildcards)
- Expose X-Request-Id for distributed tracing

---

## 5. Cookie Security

### JWT Cookie

| Attribute | Value | Purpose |
|-----------|-------|---------|
| Name | `jwt` | Standard naming |
| HttpOnly | `true` | Blocks JavaScript access (XSS mitigation) |
| Secure | `${app.security.secure-cookie}` | HTTPS-only in production |
| SameSite | `Lax` | Allows top-level navigations (OAuth redirects) |
| Path | `/` | Sent with every request |
| Max-Age | Configurable | Session or persistent |

### OAuth2 Cookies

| Cookie | SameSite | Purpose |
|--------|----------|---------|
| `oauth2_auth_request` | Lax | OAuth2 state (5 min TTL) |
| `oauth2_registration` | Strict | Registration session (10 min TTL) |
| `oauth_return_url` | Lax | Return URL (5 min TTL) |

### Guest Cookie

| Cookie | SameSite | Purpose |
|--------|----------|---------|
| `GUEST_SESSION` | Lax | Guest session ID (24h TTL) |

---

## 6. Error Handling

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handles all exceptions and returns RFC 9457 ProblemDetail responses
}
```

### Error Response Format (RFC 9457)

```json
{
  "type": "urn:shoppiq:error:not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Item with id 42 not found",
  "instance": "/items/42"
}
```

### Dual-Path Error Handling

| Request Type | 401 Response | 403 Response |
|-------------|--------------|--------------|
| Browser (`Accept: text/html`) | Redirect to `/login` | Forward to `/error` |
| API (`Accept: application/json`) | ProblemDetail (401) | ProblemDetail (403) |

### Custom Exception Hierarchy

```
ShoppiqException (base)
├── ResourceNotFoundException
├── DuplicateResourceException
├── InvalidOperationException
├── UnauthorizedAccessException
├── AiAssistantException
├── AiConversationNotFoundException
├── PaymentGatewayException
├── PaymentInvalidStateException
└── ... (50+ specific exceptions)
```

---

## 7. Docker Infrastructure

### docker-compose.yml

```yaml
services:
  qdrant:
    image: qdrant/qdrant:v1.13.0
    container_name: shoppiq-qdrant
    ports:
      - "6333:6333"   # REST API
      - "6334:6334"   # LangChain4j gRPC API
    volumes:
      - qdrant_data:/qdrant/storage
    deploy:
      resources:
        limits:
          cpus: "0.5"
          memory: 128M
    restart: unless-stopped

volumes:
  qdrant_data:
```

### Infrastructure Components

| Component | Port | Purpose |
|-----------|------|---------|
| MySQL | 3306 | Primary database (runs locally) |
| Qdrant REST | 6333 | Vector database REST API |
| Qdrant gRPC | 6334 | Vector database gRPC (LangChain4j) |

### Resource Limits

| Service | CPU | Memory |
|---------|-----|--------|
| Qdrant | 0.5 cores | 128 MB |

---

## 8. Database & Migrations

### Flyway Configuration

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
```

### Migration Naming

```
V{version}__{description}.sql
Example: V30__create_ai_chat_tables.sql
```

### Key Migrations

| Version | Description |
|---------|-------------|
| V1 | Initial schema |
| V2-V29 | Core e-commerce tables |
| V30 | AI chat tables (chat_conversations, chat_messages) |

### Database Tables

| Table | Purpose |
|-------|---------|
| `users` | User accounts |
| `roles` | User roles |
| `user_roles` | User-role mapping |
| `items` | Product catalog |
| `item_details` | Product pricing/stock |
| `categories` | Product categories |
| `orders` | Customer orders |
| `order_items` | Order line items |
| `carts` | Shopping carts |
| `cart_items` | Cart line items |
| `payments` | Payment records |
| `addresses` | User addresses |
| `sellers` | Seller profiles |
| `item_reviews` | Product reviews |
| `promo_codes` | Promotional codes |
| `promo_code_usages` | Promo usage history |
| `contact_messages` | Contact form submissions |
| `newsletter_subscribers` | Newsletter subscribers |
| `email_logs` | Email audit trail |
| `notification_preferences` | User notification settings |
| `verification_codes` | Email verification codes |
| `banners` | Homepage banners |
| `chat_conversations` | AI chat conversations |
| `chat_messages` | AI chat messages |
