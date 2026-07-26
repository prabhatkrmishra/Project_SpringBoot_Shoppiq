-- ============================================================
-- V31__add_delivery_type_to_orders.sql
--
-- Delivery type support for orders.
-- Adds a delivery_type column to the orders table so each
-- order records the shipping speed chosen at checkout:
--   NORMAL        — standard delivery (free)
--   EXPRESS_1DAY  — express 1-day delivery (additional charge)
--
-- Defaults to NORMAL for all existing orders.
-- ============================================================

ALTER TABLE orders
    ADD COLUMN delivery_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL';
