-- ============================================================
-- Shoppiq
-- V9__seed_seller_role.sql
--
-- Inserts the ROLE_SELLER role for marketplace sellers.
-- ============================================================

INSERT INTO roles (role_name)
SELECT 'ROLE_SELLER'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'ROLE_SELLER');
