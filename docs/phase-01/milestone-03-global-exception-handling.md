# Phase 1 - Foundation Refactor

# Milestone 1.3 - Global Exception Handling

---

## Objective

Redesign the application's exception handling architecture by introducing a centralized, RFC 9457-compliant error handling framework.

This milestone replaces scattered exception handling and inconsistent API responses with a unified architecture based on Spring Framework's `ProblemDetail`. From this point onward, all application errors—including business, validation, authentication, authorization, and unexpected server failures—are translated into standardized HTTP responses through a single global exception handling mechanism.

---

# Framework Research

## Spring Framework 7

### Official Recommendation

Spring Framework 7 adopts **RFC 9457 Problem Details for HTTP APIs** as the standard error response model.

Applications are encouraged to return `ProblemDetail` instead of custom error DTOs.

---

## RFC 9457

### Purpose

RFC 9457 defines a standardized JSON format for HTTP API error responses.

A typical response contains:

* HTTP status
* Title
* Detail
* Instance URI

Applications may also expose custom metadata such as:

* timestamp
* errorCode
* traceId

---

# Exception Architecture

A dedicated exception package structure was introduced.

```
src/main/java
└── com.pkmprojects.shoppiq.exception
    ├── auth
    ├── base
    ├── business
    ├── codes
    ├── constants
    ├── factory
    ├── formatter
    ├── handler
    └── validation
```

Each package has a single responsibility, providing a scalable foundation for future application modules.

---

# Core Exception Hierarchy

A common base exception was introduced.

```
ShoppiqException
```

Responsibilities:

* Stores HTTP status.
* Stores machine-readable ErrorCode.
* Stores error detail.
* Serves as the root of all application-specific exceptions.

Business exception categories were introduced:

* ResourceNotFoundException
* DuplicateResourceException
* InvalidOperationException
* UnauthorizedOperationException
* AuthenticationException

Every domain exception now extends one of these abstract base classes.

---

# Error Code Registry

A centralized `ErrorCode` enumeration was introduced.

Responsibilities:

* Stable machine-readable identifiers.
* Default human-readable messages.
* Consistent API contract.
* Decoupling API clients from Java exception class names.

Example:

```
ITEM-404-001

USER-409-001

AUTH-401-001
```

Every application exception now maps to one ErrorCode.

---

# Validation Framework

Validation failures are now standardized.

Introduced:

* ValidationException
* ValidationErrorFormatter

Responsibilities:

* Aggregate Bean Validation errors.
* Produce consistent validation messages.
* Delegate formatting away from controllers.

---

# Domain Exceptions

Legacy RuntimeExceptions were replaced with domain-specific exceptions.

Examples:

* DuplicateUserException
* ItemNotFoundException
* ItemDetailsNotFoundException
* ItemReviewNotFoundException
* OrderNotFoundException

Each exception now extends the appropriate business exception base class and carries a corresponding ErrorCode.

---

# Authentication Exceptions

Dedicated authentication exceptions were introduced.

Examples:

* AuthenticationException
* JwtAuthenticationException
* InvalidOidcUserException

Responsibilities:

* Represent authentication failures.
* Standardize JWT failures.
* Standardize OAuth2 failures.
* Integrate with Spring Security.

---

# ProblemDetailFactory

A centralized factory was introduced.

```
ProblemDetailFactory
```

Responsibilities:

* Create RFC 9457 ProblemDetail responses.
* Populate HTTP status.
* Populate title.
* Populate detail.
* Populate request URI.
* Add application metadata.

Custom properties:

* timestamp
* errorCode

Future extensions may include:

* traceId
* correlationId
* documentation URI

---

# Global Exception Handler

A centralized exception handler was introduced.

```
GlobalExceptionHandler
```

Responsibilities:

* Handle ShoppiqException.
* Handle Bean Validation failures.
* Handle unexpected exceptions.
* Produce standardized ProblemDetail responses.

Controllers no longer create error responses manually.

---

# Spring Security Integration

Security exception handling was redesigned.

Introduced:

* ShoppiqAuthenticationEntryPoint
* ShoppiqAccessDeniedHandler
* ProblemDetailResponseWriter

Responsibilities:

* Convert authentication failures into RFC 9457 responses.
* Convert authorization failures into RFC 9457 responses.
* Reuse ProblemDetailFactory.
* Serialize responses consistently through Jackson.

---

# Constants

Dedicated constants were introduced.

```
ProblemDetailProperties
```

Provides centralized names for custom ProblemDetail properties.

Example:

* timestamp
* errorCode

This removes duplicated string literals across the project.

---

# Jackson Configuration

A centralized Jackson configuration was added.

Responsibilities:

* Provide a singleton ObjectMapper.
* Ensure consistent JSON serialization.
* Support RFC 9457 ProblemDetail responses.

---

# Testing

The exception framework is fully covered by unit and MVC tests.

Implemented tests include:

* ErrorCodeTest
* ValidationErrorFormatterTest
* ProblemDetailFactoryTest
* ProblemDetailResponseWriterTest
* ShoppiqAuthenticationEntryPointTest
* ShoppiqAccessDeniedHandlerTest
* GlobalExceptionHandlerTest

Testing strategy:

* Pure JUnit
* Mockito
* WebMvcTest
* MockMvc

---

# Verification

Implementation verified the following:

* Application exceptions produce RFC 9457 ProblemDetail responses.
* Validation failures are standardized.
* Authentication failures return HTTP 401.
* Authorization failures return HTTP 403.
* Business exceptions map to correct HTTP status codes.
* ErrorCode values are consistently exposed.
* GlobalExceptionHandler processes all application exceptions.
* Security handlers integrate with the exception framework.

---

# Development Workflow

From this milestone onward:

* All business exceptions must extend ShoppiqException.
* Controllers must never manually construct business error responses.
* Services throw domain-specific exceptions.
* GlobalExceptionHandler translates exceptions into HTTP responses.
* Spring Security delegates authentication and authorization failures to the centralized handlers.

---

# Benefits

* RFC 9457 compliant API errors.
* Centralized exception handling.
* Consistent API contract.
* Machine-readable error codes.
* Improved maintainability.
* Simplified controller logic.
* Improved client integration.
* Production-ready error architecture.

---

# Architectural Decisions

## Why ProblemDetail?

ProblemDetail is the official Spring Framework implementation of RFC 9457 and provides a standardized, extensible structure for API error responses.

---

## Why a Common Base Exception?

A common exception hierarchy centralizes error metadata such as HTTP status and ErrorCode while ensuring consistent behavior across all application modules.

---

## Why Centralized ErrorCode Registry?

A single registry guarantees stable public error identifiers and prevents duplication throughout the codebase.

---

## Why Global Exception Handling?

Centralizing exception translation removes duplicated controller logic, improves maintainability, and ensures every API failure follows the same response format.

---

# Deliverables

Completed:

* Introduced ShoppiqException hierarchy.
* Added business exception categories.
* Added authentication exception hierarchy.
* Added centralized ErrorCode registry.
* Added validation framework.
* Added ProblemDetailFactory.
* Added GlobalExceptionHandler.
* Added Spring Security exception handling.
* Added ProblemDetailResponseWriter.
* Added Jackson configuration.
* Added comprehensive unit and MVC tests.
* Standardized RFC 9457 responses across the framework.

---

# Next Milestone

**Milestone 1.4 – Global Exception Handling Migration**

The next milestone will migrate the entire application to use the new exception framework by:

* Replacing legacy RuntimeExceptions.
* Refactoring controllers to remove manual error handling.
* Migrating services to domain-specific exceptions.
* Standardizing authentication flows.
* Completing application-wide RFC 9457 adoption.

---

**Author:** PrabhatKrMishra

**Version:** 1.0.0
