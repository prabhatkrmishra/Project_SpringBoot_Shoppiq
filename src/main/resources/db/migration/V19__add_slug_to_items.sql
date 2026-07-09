-- ============================================================
-- Shoppiq
-- V19__add_slug_to_items.sql
--
-- Adds slug column to items for user-friendly URLs.
-- Each product gets a unique, URL-safe identifier derived
-- from its name (e.g. "iPhone 15 Pro" -> "iphone-15-pro").
-- ============================================================

ALTER TABLE items
    ADD COLUMN slug VARCHAR(200) NOT NULL AFTER name;

ALTER TABLE items
    ADD CONSTRAINT uk_items_slug UNIQUE (slug);

CREATE INDEX idx_items_slug ON items (slug);
