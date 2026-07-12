-- ============================================================
-- Shoppiq
-- V14__add_version_to_contact_messages.sql
--
-- Adds version column to contact_messages for JPA @Version
-- optimistic locking on admin read/reply transitions.
-- ============================================================

ALTER TABLE contact_messages
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
