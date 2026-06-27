# Phase 1 - Foundation Refactor

# Milestone 1.2 - Flyway Integration

---

## Objective

Introduce Flyway as the official database migration tool for the Shoppiq project.

This milestone transitions the project from Hibernate-managed schema updates to a version-controlled database migration strategy. From this point onward, all database schema changes will be managed through Flyway migration scripts instead of relying on Hibernate's automatic schema generation.

---

# Framework Research

## Spring Boot 4.x

### Official Recommendation

Spring Boot 4 provides a dedicated Flyway starter:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
```

Using the starter enables Spring Boot's auto-configuration and version management.

---

## Flyway 11+

### Important Change

Starting with newer Flyway versions, database support is modularized.

For MySQL, an additional dependency is required:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

Without this module, Flyway cannot recognize MySQL databases and fails during application startup.

---

# Dependencies Added

## spring-boot-starter-flyway

### Purpose

Provides Spring Boot integration for Flyway.

### Responsibilities

- Auto-configures Flyway.
- Executes migrations during application startup.
- Validates migration history.
- Integrates with Spring Boot lifecycle.

### Why Chosen

Recommended by the official Spring Boot 4 documentation.

---

## flyway-mysql

### Purpose

Adds MySQL database support to Flyway.

### Responsibilities

- Registers MySQL database type.
- Enables Flyway to execute migrations on MySQL databases.

### Why Chosen

Required by Flyway 11+ for MySQL compatibility.

---

# Project Structure

The following directory structure was introduced:

```
src/main/resources
└── db
    └── migration
        └── V1__baseline.sql
```

Flyway automatically scans this directory for migration scripts during application startup.

---

# Configuration Changes

## Flyway Configuration

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
```

### Property Explanation

| Property | Purpose |
|----------|---------|
| enabled | Enables Flyway during application startup. |
| locations | Defines the location of migration scripts. |
| baseline-on-migrate | Allows Flyway to start managing an existing database. |
| validate-on-migrate | Validates applied migrations before execution. |

---

## Hibernate Configuration

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

### Why `validate`?

Hibernate now performs schema validation only.

It verifies:

- Table existence
- Column mappings
- Data types
- Relationships

Hibernate no longer creates or modifies the database schema.

This responsibility now belongs exclusively to Flyway.

---

# Baseline Migration

A baseline migration was introduced.

```
V1__baseline.sql
```

Purpose:

- Establish Flyway migration tracking.
- Mark the current schema as Version 1.
- Create the `flyway_schema_history` table.

No schema modifications were introduced in this migration.

---

# Verification

Application startup confirmed the following:

- Flyway successfully connected to the MySQL database.
- Migration validation completed successfully.
- `flyway_schema_history` table was created.
- Database schema was successfully baselined.
- Hibernate proceeded to schema validation.

This confirms that Flyway has been successfully integrated into the project.

---

# Development Workflow

From this milestone onward, every database change must be introduced through a new migration.

Example:

```
V2__create_category_table.sql

V3__create_cart_table.sql

V4__create_address_table.sql
```

No manual database modifications should be performed.

No Hibernate schema generation should be used.

---

# Benefits

- Version-controlled database schema.
- Reproducible database setup.
- Consistent deployments across environments.
- Safe schema evolution.
- Production-ready migration strategy.
- Complete migration history.

---

# Lessons Learned

## Flyway and MySQL

During implementation, Flyway reported that MySQL was unsupported.

Root Cause:

Flyway 11 separates database support into individual modules.

Resolution:

Added the `flyway-mysql` dependency alongside the Spring Boot Flyway starter.

---

# Architectural Decisions

## Why Flyway?

Flyway provides deterministic and repeatable database migrations, making schema evolution predictable across development, testing, and production environments.

---

## Why Hibernate Validation?

Hibernate is responsible only for validating entity mappings against the database schema.

Database evolution is delegated entirely to Flyway.

This separation of responsibilities improves reliability and prevents unintended schema modifications.

---

# Deliverables

Completed:

- Added Flyway support.
- Added MySQL Flyway module.
- Configured Flyway.
- Disabled Hibernate schema updates.
- Enabled schema validation.
- Created migration directory.
- Added baseline migration.
- Successfully initialized Flyway schema history.

---

# Next Milestone

**Milestone 1.3 – Global Exception Handling**

The next milestone will redesign the application's exception handling by introducing:

- RFC 9457 `ProblemDetail`
- Global exception handling
- Standardized API error responses
- Validation error handling
- Custom exception hierarchy

---

**Author:** PrabhatKrMishra

**Version:** 1.0.0