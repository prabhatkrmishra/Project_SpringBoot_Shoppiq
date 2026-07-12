-- ============================================================
-- Shoppiq
-- V11__add_publishing_status_to_items.sql
--
-- Adds publishing_status column to items for seller product
-- lifecycle management (DRAFT, PENDING, PUBLISHED).
-- Existing admin-created items are backfilled as PUBLISHED.
-- ============================================================

ALTER TABLE items
    ADD COLUMN publishing_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- Existing catalog items (legacy, admin-created) are already live
UPDATE items
SET publishing_status = 'PUBLISHED'
WHERE publishing_status = 'DRAFT';
