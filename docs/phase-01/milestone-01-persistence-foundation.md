# Phase 1 - Foundation Refactor

## Milestone 1.1 - Persistence Foundation

### Objective

Create a reusable persistence layer that will be shared by every JPA entity.

---

## Files Added

### BaseEntity

**Purpose**

Provides common persistence properties.

**Responsibilities**

- Database generated primary key.
- Optimistic locking support.

---

### AuditableEntity

**Purpose**

Adds automatic auditing to all entities.

**Responsibilities**

- Tracks entity creation time.
- Tracks entity modification time.

---

### JpaAuditConfig

**Purpose**

Enables Spring Data JPA auditing.

---

## Design Decisions

### Why BaseEntity?

Avoid duplicate ID and version fields across entities.

### Why AuditableEntity?

Keeps auditing separate from persistence identity.

### Why Instant?

UTC timestamps eliminate timezone ambiguity and are recommended for backend persistence.

### Why @MappedSuperclass?

Allows common fields to be inherited without creating unnecessary database tables.

---

## Future Dependencies

Every future entity in Shoppiq will extend:

AuditableEntity
↓
BaseEntity

---

## Next Milestone

Milestone 1.2 - Flyway Integration