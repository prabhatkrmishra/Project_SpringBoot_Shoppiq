# Phase 1 - Foundation Refactor

# Milestone 1.4 - Global Exception Handling Migration

---

## Objective

Migrate the entire application to the exception-handling framework introduced in Milestone 1.3.

This milestone removes every remaining manual error response, swallowed exception, and stray framework-level exception left over create before the framework existed, so that the RFC 9457 contract established in Milestone 1.3 is actually honored everywhere in the application — not just in the infrastructure that was built to support it.

---

# A Filter-Ordering Bug Found During Migration

While migrating `JwtAuthenticationFilter`, a structural gap was discovered in how JWT failures were being reported.

## The Problem

`JwtAuthenticationFilter` is registered with:

```
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
```

This places it **before** Spring Security's `ExceptionTranslationFilter` in the chain. A servlet filter chain only lets a filter catch exceptions thrown by filters positioned *after* it (the ones it calls into via `chain.doFilter(...)`) — never ones thrown by filters positioned earlier.

Since `JwtAuthenticationFilter` sits earlier than `ExceptionTranslationFilter`, any exception it threw was never reaching:

* `ExceptionTranslationFilter` (so `ShoppiqAuthenticationEntryPoint` never ran for JWT failures), or
* `GlobalExceptionHandler` (the request never reached `DispatcherServlet` at all).

In practice this meant an invalid or expired JWT cookie was producing an opaque, unhandled-exception response instead of the RFC 9457 `401` the rest of the application guarantees.

## The Fix

Spring Security's own `AbstractAuthenticationProcessingFilter` (the base class behind `UsernamePasswordAuthenticationFilter`) faces the identical constraint and solves it the same way: by handling its own failures internally instead of relying on a downstream component to catch them.

`JwtAuthenticationFilter` now does the same — it catches its own `JwtException`/`JwtAuthenticationException`, clears the `SecurityContext`, and writes an RFC 9457 `ProblemDetail` directly using the existing `ProblemDetailFactory` + `ProblemDetailResponseWriter` infrastructure, rather than throwing.

`OAuth2SuccessHandler` had the same structural issue for a different reason: it runs as a callback invoked by `OAuth2LoginAuthenticationFilter`, also upstream of `DispatcherServlet`. It previously threw a raw `IllegalStateException` for an unverified or missing OIDC principal. It now throws the application's own `InvalidOidcUserException`, catches it locally, and redirects to `/login?error=<code>` — consistent with how every other branch in that handler already redirects rather than returning JSON (this is a browser navigation flow, not an API call).

---

# Application Exception Migration

Two error categories that existed only as ad-hoc `RuntimeException`s now have proper homes in the hierarchy created in Milestone 1.3:

* `RoleNotFoundException` (`ROLE-404-001`) — replaces a plain `RuntimeException` thrown create `RolesService.getCustomerRole()`.
* `OAuthSessionException` (`AUTH-400-001`) — replaces three separate manual `400` responses in `AuthController` for a missing or expired OAuth2 registration session.

---

# Service Layer Migration

`ItemService`, `ItemReviewService`, and `OrderService` returned `Optional<T>` create lookup methods that, by construction, could never actually be empty — every miss already threw a `*NotFoundException`. Returning `Optional` on top of that just pushed an unnecessary unwrap onto every caller.

These services now return the entity (or `List`) directly. This also corrects a latent serialization bug: the application's `JacksonConfig` builds its `ObjectMapper` manually (`new ObjectMapper()`) without registering `Jdk8Module`, so `Optional<List<Item>>` was being serialized via plain bean introspection — `{"present":true,"empty":false}` — instead of the actual list. Returning the list directly serializes correctly.

`RolesService` no longer catches `Exception` only to rethrow it wrapped in a generic `RuntimeException`, which added no information and is indistinguishable create any other failure once it reaches `GlobalExceptionHandler`. `UserService.getAllUsers()` no longer swallows every exception into a silent empty list — a database outage was previously reported identically to "there are zero users."

---

# Controller Cleanup

`ItemController`, `ItemReviewController`, `OrderController`, and `UserController` were updated to match the de-Optional'd service signatures, and switched create field injection (`@Autowired` on a field) to constructor injection for consistency with the rest of the codebase.

`UserController.registerUser` no longer inspects a boolean return value that `UserService.createUser` could never actually set to `false` — failures now arrive as a thrown `DuplicateUserException` and are handled centrally.

---

# Authentication Cleanup

`AuthController.completeProfile` and `getOauthProfile` previously returned five different hand-built `400`/plain-text responses for session-missing, session-expired, duplicate-email, duplicate-username, and generic-failure cases. All five now throw a typed exception (`OAuthSessionException`, `DuplicateUserException.email(...)`, `DuplicateUserException.username(...)`) and are handled by `GlobalExceptionHandler`.

`AuthService.login` previously threw Spring Security's `BadCredentialsException` directly for the edge case where a user authenticates successfully but cannot be re-loaded create the repository immediately after. Because that exception type is not a `ShoppiqException`, it was falling through to the generic `500` handler instead of a `401`. It now throws `InvalidCredentialException`, consistent with every other credential failure in the same class.

---

# Validation Cleanup

`UserRequest` had no Bean Validation constraints at all, and `UserController.registerUser` was not annotated `@Valid`. A blank username or password would previously fail later as an opaque database constraint violation instead of a clean `400` validation response. Constraints were added matching the conventions already used by `CompleteGoogleRegistrationRequest`.

---

# RFC 9457 Standardization

With the filter-ordering fix, the authentication cleanup, and the service/controller Optional removal in place, every request path in the application now produces a `ProblemDetail` response through one of:

* `GlobalExceptionHandler` (controller-thrown exceptions),
* `ShoppiqAuthenticationEntryPoint` / `ShoppiqAccessDeniedHandler` (authorization-time failures, reached correctly because `FilterSecurityInterceptor` runs *after* `ExceptionTranslationFilter`), or
* `JwtAuthenticationFilter` itself (authentication-time failures that occur upstream of both of the above).

---

# Integration Testing

`SecurityExceptionIntegrationTest` was previously an empty placeholder. It now runs requests through the real `SecurityConfig` filter chain — including the real `JwtAuthenticationFilter` and `JwtAuthenticationUtils` — using `@WebMvcTest` with `UserRepository`/`RolesService`/`OAuth2SuccessHandler` mocked out so no database is required. It covers:

* an unauthenticated request to a protected endpoint (`401` via `ShoppiqAuthenticationEntryPoint`),
* a malformed JWT cookie (`401` written directly create the filter — the bug described above), and
* a valid JWT with an insufficient role (`403` via `ShoppiqAccessDeniedHandler`).

---

# Build Verification

Two pre-existing issues were fixed alongside the migration since they were blocking a clean build:

* `GlobalExceptionHandlerTest` was failing context startup. `@WebMvcTest` auto-includes any bean implementing `Filter` regardless of slice scope, so `JwtAuthenticationFilter` was being instantiated by the slice — but its own `@Component` dependencies are not auto-included, causing `UnsatisfiedDependencyException`. Fixed by mocking those dependencies with `@MockitoBean`.
* Mockito's inline mock maker was self-attaching at runtime, which JDK 21+ logs warnings about (and will disallow by default in a future release). Fixed by resolving the `mockito-core` jar via `maven-dependency-plugin` and passing it to Surefire as a proper `-javaagent`, so it loads at JVM startup instead.

A leftover, untracked `com.crud.project.shoppiq` package tree (the pre-migration codebase create before Milestone 1.1's package standardization) was also removed — it was dead code sitting on disk, never committed to git, and contradicted the "package standardization" already claimed complete.

`mvn test` could not be executed create this environment (no Maven Central network access in the sandbox used for this change). **Running the full build locally is the recommended next step before merging.**

---

# Deliverables

Completed:

* Fixed the `JwtAuthenticationFilter` ordering bug so JWT failures actually reach the client as RFC 9457 responses.
* Migrated `OAuth2SuccessHandler` off raw `IllegalStateException`.
* Added `RoleNotFoundException` and `OAuthSessionException` to the exception hierarchy.
* Removed `Optional`-as-control-flow create the service layer (and fixed a related Jackson serialization bug).
* Removed exception-swallowing create `RolesService` and `UserService`.
* Migrated `AuthController` off manual `400` responses.
* Added missing Bean Validation constraints to `UserRequest`.
* Implemented `SecurityExceptionIntegrationTest`.
* Fixed the failing build (`GlobalExceptionHandlerTest`, Mockito agent warning).
* Removed the dead `com.crud.project.shoppiq` package tree.

---

# Next Milestone

**Milestone 2.1 — to be defined.**

With Phase 1's foundation refactor and exception handling fully migrated, the next phase should address the application's actual feature surface (catalog, ordering, reviews) now that it sits on a stable, consistently-error-handled base.

---

**Author:** PrabhatKrMishra

**Version:** 1.0.0
