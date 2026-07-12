-- ============================================================
-- Shoppiq
-- V12__add_review_status.sql
--
-- Adds moderation status to item_reviews (PENDING, APPROVED,
-- REJECTED). Only APPROVED reviews are publicly visible;
-- PENDING reviews are visible only to the author.
-- ============================================================

ALTER TABLE item_reviews
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING' AFTER review;
