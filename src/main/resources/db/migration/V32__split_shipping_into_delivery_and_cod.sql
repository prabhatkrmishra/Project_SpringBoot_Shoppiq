-- ============================================================
-- V32__split_shipping_into_delivery_and_cod.sql
--
-- Splits the single shipping_fee column into two separate
-- charges so each can be tracked and displayed independently:
--
--   delivery_charge — $7.50 for EXPRESS_1DAY, $0 for NORMAL
--   cod_surcharge   — $5.00 for COD orders, $0 otherwise
--
-- Backfills existing orders using payment_method and
-- delivery_type, then drops the old shipping_fee column.
-- ============================================================

-- Add the two new columns
ALTER TABLE orders
    ADD COLUMN delivery_charge DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN cod_surcharge   DECIMAL(10, 2) NOT NULL DEFAULT 0.00;

-- Backfill: express delivery orders → $7.50 delivery charge
UPDATE orders
SET delivery_charge = 7.50
WHERE delivery_type = 'EXPRESS_1DAY';

-- Backfill: COD orders → $5.00 surcharge
UPDATE orders
SET cod_surcharge = 5.00
WHERE payment_method = 'COD';

-- Drop the old combined column
ALTER TABLE orders DROP COLUMN shipping_fee;
