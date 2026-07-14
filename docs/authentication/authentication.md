# Authentication & Authorization Service

> **Framework:** Spring Boot 7.1 + JWT (JJWT 0.13.0) + OAuth2 Client
> **Cookie-Only JWT:** Token never exposed to JavaScript — HttpOnly, SameSite=Lax
> **State Management:** Fully stateless — no HttpSession created

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Key Components](#2-key-components)
3. [Thymeleaf Templates](#3-thymeleaf-templates)
4. [JWT Token System](#4-jwt-token-system)
5. [OAuth2 Google Login](#5-oauth2-google-login)
6. [CSRF Protection](#6-csrf-protection)
7. [Role-Based Access Control](#7-role-based-access-control)
8. [Filter Chain Order](#8-filter-chain-order)
9. [Error Handling](#9-error-handling)
10. [Account Lockout](#10-account-lockout)

---

## 1. Architecture Overview

### Controller Layer

| Controller | Path | Endpoints | Description |
|------------|------|-----------|-------------|
| `AuthController` | `auth/controller/AuthController.java` | 4 | Login, logout, OAuth2 profile, token refresh |

### Service Layer

| Service | Path | Description |
|---------|------|-------------|
| `AuthService` | `auth/service/AuthService.java` | Credential validation, JWT generation, logout |
| `CustomUserDetailService` | `auth/service/CustomUserDetailService.java` | UserDetailsService implementation |
| `JwtAuthenticationUtils` | `auth/utils/JwtAuthenticationUtils.java` | Token generation, validation, claim extraction |
| `JwtCookieFactory` | `auth/utils/JwtCookieFactory.java` | Cookie building with HttpOnly, Secure, SameSite |
| `OAuthRegistrationCookieService` | `auth/oauth2/OAuthRegistrationCookieService.java` | OAuth registration session cookie management |

### Filter Layer

| Filter | Path | Purpose |
|--------|------|---------|
| `JwtAuthenticationFilter` | `auth/jwt/JwtAuthenticationFilter.java` | JWT extraction, validation, authentication |
| `OAuthReturnUrlFilter` | `auth/oauth2/OAuthReturnUrlFilter.java` | Captures return URL before OAuth redirect |
| `RateLimitFilter` | `filter/RateLimitFilter.java` | Rate limiting for sensitive endpoints |

### OAuth2 Components

| Class | Path | Description |
|-------|------|-------------|
| `HttpCookieOAuth2AuthorizationRequestRepository` | `auth/oauth2/` | OAuth2 state cookie storage |
| `OAuth2SuccessHandler` | `auth/oauth2/OAuth2SuccessHandler.java` | Login success branching logic |

---

## 2. Key Components

### AuthController Endpoints

| Method | Endpoint | Service Method | Description |
|--------|----------|---------------|-------------|
| `POST` | `/auth/login` | `authService.login()` | Username/password login, sets JWT cookie |
| `POST` | `/auth/logout` | `authService.logout()` | Clears JWT cookie, returns success message |
| `GET` | `/auth/google/get-profile` | `registrationCookieService.read()` | Returns OAuth profile for pre-fill |
| `POST` | `/auth/google/complete-profile` | `userService.createGoogleUser()` | Creates user and sets JWT cookie |
| `POST` | `/auth/refresh` | `jwtAuthenticationUtils.validateTokenForRefresh()` | Refreshes expired tokens if within max age |

### AuthService Methods

| Method | Description |
|--------|-------------|
| `login(JwtRequest, HttpServletResponse)` | Validates credentials via AuthenticationManager, generates JWT, adds cookie |
| `logout(HttpServletResponse)` | Expires JWT cookie (Max-Age=0) |
| `authenticate(String, String)` | Validates credentials, tracks failed attempts, locks account after 5 failures |

### JwtAuthenticationUtils Methods

| Method | Description |
|--------|-------------|
| `generateToken(User, long)` | Creates HMAC-signed JWT with userId, username, roles, tokenVersion claims |
| `validateToken(String, User)` | Checks signature, expiry, tokenVersion, enabled status |
| `validateTokenForRefresh(String, User, long)` | Same as validate + max age check (30 days) |
| `extractJwtFromCookies(HttpServletRequest)` | Reads JWT from `jwt` cookie |
| `getUsernameFromToken(String)` | Extracts `sub` claim |
| `getUserIdFromToken(String)` | Extracts `userId` claim |
| `getRolesFromToken(String)` | Extracts `roles` claim |
| `getTokenVersionFromToken(String)` | Extracts `tokenVersion` claim |

### JwtCookieFactory Methods

| Method | Description |
|--------|-------------|
| `buildJwtCookie(String, int)` | Creates cookie with name=`jwt`, HttpOnly=true, SameSite=Lax, Secure=prod |

---

## 3. Thymeleaf Templates

| Template | URL | Controller Mapping | Purpose |
|----------|-----|-------------------|---------|
| `login.html` | `/login` | `FrontEndController.loginPage()` | Login form |
| `register.html` | `/register` | `FrontEndController.registerPage()` | Registration form |
| `completeprofile.html` | `/complete-profile` | `FrontEndController.completeProfilePage()` | OAuth registration completion |
| `forgot-password.html` | `/forgot-password` | `FrontEndController.forgotPasswordPage()` | Password reset request |
| `reset-password.html` | `/reset-password` | `FrontEndController.resetPasswordPage()` | Password reset form |

---

## 4. JWT Token System

### Token Claims

| Claim | Source | Purpose |
|-------|--------|---------|
| `sub` | `user.getUsername()` | Subject identifier |
| `userId` | `user.getId()` (Long) | Database PK for single-query validation |
| `roles` | `user.getAuthorities()` → `List<String>` | Role strings like `"ROLE_CUSTOMER"` |
| `tokenVersion` | `user.getTokenVersion()` (Integer) | Invalidation mechanism |
| `iat` / `exp` | `System.currentTimeMillis()` | Issued-at and expiration |

### Token Delivery (Cookie-Only)

The JWT is **never returned in the response body**. It is delivered exclusively as an HttpOnly cookie:

```
Cookie attributes:
  Name:     jwt
  HttpOnly: true         (blocks JavaScript access - XSS mitigation)
  Secure:   ${app.security.secure-cookie:true}  (HTTPS-only in prod)
  SameSite: Lax          (allows top-level navigations like OAuth redirects)
  Path:     /            (sent with every request)
  Max-Age:  -1 (session) | 0 (expire) | >0 (persistent)
```

### Token Validation Flow

1. **Signature + Expiry** — JJWT-level verification
2. **Username match** — `sub` claim vs database
3. **Token version match** — `tokenVersion` claim vs database (single DB query)
4. **Account enabled** — `user.isEnabled()` must be true

---

## 5. OAuth2 Google Login

### Complete Flow

| Step | Component | Action |
|------|-----------|--------|
| 1 | Browser | `GET /oauth2/authorization/google?returnUrl=/some-page` |
| 2 | OAuthReturnUrlFilter | Captures returnUrl → saves in `oauth_return_url` cookie (5 min TTL) |
| 3 | HttpCookieOAuth2AuthorizationRequestRepository | Saves OAuth2 state as HMAC-signed JSON → `oauth2_auth_request` cookie |
| 4 | Google | Redirects to consent screen |
| 5 | Google | Redirects back with code + state |
| 6 | Spring Security | Validates state, exchanges code |
| 7 | OAuth2SuccessHandler | Branches to existing user or new user flow |
| 8 | Frontend | `GET /auth/google/get-profile` reads registration cookie |
| 9 | Frontend | `POST /auth/google/complete-profile` with username/password |

### OAuth2 Cookies

| Cookie | Purpose | TTL | SameSite |
|--------|---------|-----|----------|
| `oauth2_auth_request` | OAuth2 state | 5 min | Lax |
| `oauth2_registration` | Google profile for registration | 10 min | Strict |
| `oauth_return_url` | Redirect destination | 5 min | Lax |

---

## 6. CSRF Protection

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

### Exemption Rationale

| Exempt Endpoint | Reason |
|-----------------|--------|
| Auth endpoints | Public, no session to forge |
| OAuth2 endpoints | Protected by OAuth2 state parameter |
| Newsletter/banners | GET-safe or public |

---

## 7. Role-Based Access Control

### Roles

| Role | Description |
|------|-------------|
| `ROLE_CUSTOMER` | Shopping, reviews, checkout, AI chat |
| `ROLE_SELLER` | Seller profile, product management, order tracking |
| `ROLE_ADMIN` | Full admin access |

### Authorization Rules

| Endpoint Pattern | Access |
|------------------|--------|
| `/`, `/login`, `/register`, `/oauth2/**` | `permitAll()` |
| `/cart` | `hasRole("CUSTOMER")` |
| `/address` | `hasAnyRole("CUSTOMER", "SELLER")` |
| `/admin/**`, `/api/admin/**` | `hasRole("ADMIN")` |
| `/seller/**` | `hasAnyRole("SELLER", "ADMIN")` |
| `/user/cart/**`, `/user/order/**` | `hasAnyRole("CUSTOMER", "ADMIN")` |

---

## 8. Filter Chain Order

```
RateLimitFilter (before JWT filter)
    ↓
OAuthReturnUrlFilter (before OAuth2 redirect)
    ↓
JwtAuthenticationFilter (before UsernamePassword)
    ↓
Spring Security standard filters
```

### JWT Filter Bypass

`JwtAuthenticationFilter.shouldNotFilter()` skips processing on:
- All authentication endpoints (`/auth/**`)
- OAuth2 endpoints (`/oauth2/**`)
- Static assets
- Public APIs (`/api/banners/active`, `/api/ai/guest/**`)

---

## 9. Error Handling

### Dual-Path Error Handling

| Request Type | 401 Response | 403 Response |
|--------------|--------------|--------------|
| Browser | Redirect to `/login?returnUrl=...` | Forward to `/error` |
| API | RFC 9457 `ProblemDetail` | RFC 9457 `ProblemDetail` |

---

## 10. Account Lockout

### Configuration

| Setting | Value |
|---------|-------|
| Max failed attempts | 5 |
| Lockout duration | 30 minutes |
| Auto-unlock | Yes |

### Behavior

| Attempt Count | Action |
|---------------|--------|
| 1-4 failures | Tracks `failedLoginAttempts` |
| 5 failures | Sets `lockoutTime`, throws lock error |
| After 30 min | `isAccountNonLocked()` returns true |

### Remember Me

The `JwtRequest.rememberMe` flag controls cookie persistence:
- `true` — persistent cookie with Max-Age = expiration (long-lived, up to 30 days)
- `false` — session cookie (browser tab close deletes it)
