-- ============================================================
-- Shoppiq
-- V33__add_coupon_type_to_promo_codes.sql
--
-- Adds coupon_type (SINGLE / BULK) and min_item_quantity columns
-- to promo_codes to support cart composition constraints.
-- ============================================================

ALTER TABLE promo_codes
    ADD COLUMN coupon_type VARCHAR(10);

ALTER TABLE promo_codes
    ADD COLUMN min_item_quantity INT;

CREATE INDEX idx_promo_codes_coupon_type
    ON promo_codes (coupon_type);
