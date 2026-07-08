-- ============================================================
-- Shoppiq
-- V18__add_image_url_to_item_details.sql
--
-- Adds image_url column to item_details so each product
-- variant can store a URL to its product image.
-- ============================================================

ALTER TABLE item_details
    ADD COLUMN image_url VARCHAR(500) AFTER discount_percentage;
