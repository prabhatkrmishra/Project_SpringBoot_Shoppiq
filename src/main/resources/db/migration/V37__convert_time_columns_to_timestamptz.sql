-- ============================================================
-- Shoppiq
-- V37__convert_time_columns_to_timestamptz.sql
--
-- Converts three DATETIME columns to TIMESTAMP so they are
-- stored as UTC epoch-offsets, consistent with the
-- java.time.Instant mapping used by the rest of the schema.
--
-- The affected columns:
--   - users.email_verified_at
--   - users.lockout_time
--   - verification_codes.expires_at
--
-- MySQL TIMESTAMP is always stored in UTC. Existing values are
-- interpreted in the session timezone and converted to UTC
-- during the MODIFY, so the session must be set to UTC
-- (as it is in production) for a zero-loss migration.
-- ============================================================

ALTER TABLE users
    MODIFY COLUMN email_verified_at TIMESTAMP NULL;

ALTER TABLE users
    MODIFY COLUMN lockout_time TIMESTAMP NULL;

ALTER TABLE verification_codes
    MODIFY COLUMN expires_at TIMESTAMP NULL;
