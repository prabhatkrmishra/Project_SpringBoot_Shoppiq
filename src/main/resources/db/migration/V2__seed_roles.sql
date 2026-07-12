-- ============================================================
-- Shoppiq
-- V2__seed_roles.sql
--
-- Seeds the base application roles (ROLE_ADMIN, ROLE_CUSTOMER).
-- ============================================================

INSERT INTO roles (role_name)
VALUES ('ROLE_ADMIN'),
       ('ROLE_CUSTOMER');