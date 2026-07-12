-- ============================================================
-- Shoppiq
-- V25__add_version_to_email_logs.sql
--
-- Adds version column to email_logs and verification_codes
-- for JPA @Version optimistic locking.
-- ============================================================

ALTER TABLE email_logs
    ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE verification_codes
    ADD COLUMN version BIGINT DEFAULT 0;
