-- V25__add_version_to_email_logs.sql
-- Add missing version column for optimistic locking in email_logs and verification_codes

ALTER TABLE email_logs ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE verification_codes ADD COLUMN version BIGINT DEFAULT 0;
