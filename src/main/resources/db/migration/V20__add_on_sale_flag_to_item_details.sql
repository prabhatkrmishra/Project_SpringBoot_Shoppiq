-- ============================================================
-- Shoppiq
-- V20__add_on_sale_flag_to_item_details.sql
--
-- Adds on_sale flag to item_details so admin can mark
-- specific products for promotional sale events.
-- ============================================================

ALTER TABLE item_details
    ADD COLUMN on_sale BOOLEAN NOT NULL DEFAULT FALSE AFTER image_url;
