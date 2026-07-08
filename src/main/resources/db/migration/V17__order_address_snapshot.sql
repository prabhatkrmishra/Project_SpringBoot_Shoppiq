-- ============================================================
-- Shoppiq
-- V17__order_address_snapshot.sql
--
-- Makes address_id nullable and changes FK from RESTRICT to
-- SET NULL so users can delete addresses without losing orders.
-- Adds shipping address snapshot columns to orders so each
-- order retains the address as it was at checkout time.
-- ============================================================

-- Make address_id nullable before changing FK to SET NULL
ALTER TABLE orders MODIFY COLUMN address_id BIGINT NULL;

-- Drop the old RESTRICT FK first
ALTER TABLE orders DROP FOREIGN KEY fk_orders_address;

-- Re-add with SET NULL
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_address
        FOREIGN KEY (address_id) REFERENCES addresses (id) ON DELETE SET NULL;

-- Add address snapshot columns so orders retain the address at time of placement
ALTER TABLE orders
    ADD COLUMN shipping_full_name VARCHAR(100) AFTER address_id,
    ADD COLUMN shipping_phone     VARCHAR(15)  AFTER shipping_full_name,
    ADD COLUMN shipping_line1     VARCHAR(255) AFTER shipping_phone,
    ADD COLUMN shipping_line2     VARCHAR(255) AFTER shipping_line1,
    ADD COLUMN shipping_city      VARCHAR(100) AFTER shipping_line2,
    ADD COLUMN shipping_state     VARCHAR(100) AFTER shipping_city,
    ADD COLUMN shipping_postal_code VARCHAR(10) AFTER shipping_state,
    ADD COLUMN shipping_country   VARCHAR(100) AFTER shipping_postal_code;
