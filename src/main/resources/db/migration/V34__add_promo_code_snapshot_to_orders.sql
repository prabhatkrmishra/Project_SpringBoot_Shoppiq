-- ============================================================
-- Shoppiq
-- V34__add_promo_code_snapshot_to_orders.sql
--
-- Adds a denormalized promo_code_snapshot column to orders
-- so that the promo code string is preserved on the order
-- itself, independent of the promo_codes table.
--
-- Why:
--   Previously, Order.promoCode was a FK to promo_codes.
--   If a promo code was deleted or deactivated, historical
--   orders lost visibility into which code was applied.
--   The snapshot column decouples order history from the
--   promo_codes lifecycle.
--
-- What changes:
--   1. Adds promo_code_snapshot VARCHAR(50) NULL to orders.
--   2. Backfills existing orders that have a promo_code_id
--      by JOINing against promo_codes to copy the code string.
--
-- Downstream impact:
--   - CheckoutServiceImpl now writes to this column instead
--     of relying solely on the FK.
--   - CheckoutResponse reads from this column.
--   - The promo_code_id FK is retained for referential
--     integrity but is no longer required for reads.
-- ============================================================

-- Add the denormalized snapshot column
ALTER TABLE orders
    ADD COLUMN promo_code_snapshot VARCHAR(50) NULL;

-- Backfill: copy the code string from promo_codes for
-- all orders that have an applied promo code
UPDATE orders o
    JOIN promo_codes pc
ON pc.id = o.promo_code_id
    SET o.promo_code_snapshot = pc.code
WHERE o.promo_code_id IS NOT NULL;
