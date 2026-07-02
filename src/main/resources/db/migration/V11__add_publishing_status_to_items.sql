-- ============================================================
-- V12: Add publishing status to items for seller product lifecycle
-- ============================================================

ALTER TABLE items
    ADD COLUMN publishing_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- Existing catalog items (legacy, admin-created) are already live
UPDATE items
SET publishing_status = 'PUBLISHED'
WHERE publishing_status = 'DRAFT';
